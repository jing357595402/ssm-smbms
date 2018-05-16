package cn.smbms.controller;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.mysql.jdbc.StringUtils;

import cn.smbms.pojo.Role;
import cn.smbms.pojo.User;
import cn.smbms.service.role.RoleService;
import cn.smbms.service.user.UserService;
import cn.smbms.tools.Constants;
import cn.smbms.tools.PageSupport;

@Controller
@RequestMapping("/sys/user")
public class UserController extends BaseController{
	private Logger logger = Logger.getLogger(UserController.class);
	
	@Resource
	private UserService userService;
	@Resource
	private RoleService roleService;

	@RequestMapping(value="/list.html")
	public String getUserList(Model model,
							@RequestParam(value="queryname",required=false) String queryUserName,
							@RequestParam(value="queryUserRole",required=false) String queryUserRole,
							@RequestParam(value="pageIndex",required=false) String pageIndex){
		logger.info("getUserList ---- > queryUserName: " + queryUserName);
		logger.info("getUserList ---- > queryUserRole: " + queryUserRole);
		logger.info("getUserList ---- > pageIndex: " + pageIndex);
		Integer _queryUserRole = null;		
		List<User> userList = null;
		List<Role> roleList = null;
		//设置页面容量
    	int pageSize = Constants.pageSize;
    	//当前页码
    	int currentPageNo = 1;
	
		if(queryUserName == null){
			queryUserName = "";
		}
		if(queryUserRole != null && !queryUserRole.equals("")){
			_queryUserRole = Integer.parseInt(queryUserRole);
		}
		
    	if(pageIndex != null){
    		try{
    			currentPageNo = Integer.valueOf(pageIndex);
    		}catch(NumberFormatException e){
    			return "redirect:/syserror.html";
    		}
    	}	
    	//总数量（表）	
    	int totalCount = 0;
		try {
			totalCount = userService.getUserCount(queryUserName,_queryUserRole);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	//总页数
    	PageSupport pages=new PageSupport();
    	pages.setCurrentPageNo(currentPageNo);
    	pages.setPageSize(pageSize);
    	pages.setTotalCount(totalCount);
    	int totalPageCount = pages.getTotalPageCount();
    	//控制首页和尾页
    	if(currentPageNo < 1){
    		currentPageNo = 1;
    	}else if(currentPageNo > totalPageCount){
    		currentPageNo = totalPageCount;
    	}
		try {
			userList = userService.getUserList(queryUserName,_queryUserRole,currentPageNo,pageSize);
			
			roleList = roleService.getRoleList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		model.addAttribute("userList", userList);
		model.addAttribute("roleList", roleList);
		model.addAttribute("queryUserName", queryUserName);
		model.addAttribute("queryUserRole", queryUserRole);
		model.addAttribute("totalPageCount", totalPageCount);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("currentPageNo", currentPageNo);
		return "userlist";
	}
	
	@RequestMapping(value="/add.html",method=RequestMethod.GET)
	public String addUser(@ModelAttribute("user") User user){
		return "useradd";
	}
	
	//多文件上传
	@RequestMapping(value="/addsave.html",method=RequestMethod.POST)
	public String addUserSave(User user,HttpSession session,HttpServletRequest request,
							 @RequestParam(value ="attachs", required = false) MultipartFile[] attachs){
		String idPicPath = null;
		String workPicPath = null;
		String errorInfo = null;
		boolean flag = true;
		String path = request.getSession().getServletContext().getRealPath("statics"+File.separator+"uploadfiles"); 
		logger.info("uploadFile path ============== > "+path);
		for(int i = 0;i < attachs.length ;i++){
			MultipartFile attach = attachs[i];
			if(!attach.isEmpty()){
				if(i == 0){
					errorInfo = "uploadFileError";
				}else if(i == 1){
					errorInfo = "uploadWpError";
	        	}
				String oldFileName = attach.getOriginalFilename();//原文件名
				logger.info("uploadFile oldFileName ============== > "+oldFileName);
				String prefix=FilenameUtils.getExtension(oldFileName);//原文件后缀     
		        logger.debug("uploadFile prefix============> " + prefix);
				int filesize = 500000;
				logger.debug("uploadFile size============> " + attach.getSize());
		        if(attach.getSize() >  filesize){//上传大小不得超过 500k
	            	request.setAttribute(errorInfo, " * 上传大小不得超过 500k");
	            	flag = false;
	            }else if(prefix.equalsIgnoreCase("jpg") || prefix.equalsIgnoreCase("png") 
	            		|| prefix.equalsIgnoreCase("jpeg") || prefix.equalsIgnoreCase("pneg")){//上传图片格式不正确
	            	String fileName = System.currentTimeMillis()+RandomUtils.nextInt(1000000)+"_Personal.jpg";  
	                logger.debug("new fileName======== " + attach.getName());
	                File targetFile = new File(path, fileName);  
	                if(!targetFile.exists()){  
	                    targetFile.mkdirs();  
	                }  
	                //保存  
	                try {  
	                	attach.transferTo(targetFile);  
	                } catch (Exception e) {  
	                    e.printStackTrace();  
	                    request.setAttribute(errorInfo, " * 上传失败！");
	                    flag = false;
	                }  
	                if(i == 0){
	                	 idPicPath = path+File.separator+fileName;
	                }else if(i == 1){
	                	 workPicPath = path+File.separator+fileName;
	                }
	                logger.debug("idPicPath: " + idPicPath);
	                logger.debug("workPicPath: " + workPicPath);
	                
	            }else{
	            	request.setAttribute(errorInfo, " * 上传图片格式不正确");
	            	flag = false;
	            }
			}
		}
		if(flag){
			user.setCreatedBy(((User)session.getAttribute(Constants.USER_SESSION)).getId());
			user.setCreationDate(new Date());
			user.setIdPicPath(idPicPath);
			user.setWorkPicPath(workPicPath);
			try {
				if(userService.add(user)){
					return "redirect:/sys/user/list.html";
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "useradd";
	}
	
	@RequestMapping(value="/modify/{id}",method=RequestMethod.GET)
	public String getUserById(@PathVariable String id,Model model,HttpServletRequest request){
		User user = new User();
		try {
			user = userService.getUserById(Integer.parseInt(id));
			if(user.getIdPicPath() != null && !"".equals(user.getIdPicPath())){
				String[] paths = user.getIdPicPath().split("\\"+File.separator);
				logger.debug("view picPath paths[paths.length-1]============ " + paths[paths.length-1]);
				user.setIdPicPath(request.getContextPath()+"/statics/uploadfiles/"+paths[paths.length-1]);
			}
			if(user.getWorkPicPath() != null && !"".equals(user.getWorkPicPath())){
				String[] paths = user.getWorkPicPath().split("\\"+File.separator);
				logger.debug("view workPicPath paths[paths.length-1]============ " + paths[paths.length-1]);
				user.setWorkPicPath(request.getContextPath()+"/statics/uploadfiles/"+paths[paths.length-1]);
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		model.addAttribute(user);
		return "usermodify";
	}
	
	@RequestMapping(value="/modifysave.html",method=RequestMethod.POST)
	public String modifyUserSave(User user,HttpSession session,HttpServletRequest request,
								@RequestParam(value ="attachs", required = false) MultipartFile[] attachs){
		logger.debug("modifyUserSave id===================== "+user.getId());
		String idPicPath = null;
		String workPicPath = null;
		String errorInfo = null;
		boolean flag = true;
		String path = request.getSession().getServletContext().getRealPath("statics"+File.separator+"uploadfiles"); 
		logger.info("uploadFile path ============== > "+path);
		if(attachs != null){
			for(int i = 0;i < attachs.length ;i++){
				MultipartFile attach = attachs[i];
				if(!attach.isEmpty()){
					if(i == 0){
						errorInfo = "uploadFileError";
					}else if(i == 1){
						errorInfo = "uploadWpError";
		        	}
					String oldFileName = attach.getOriginalFilename();//原文件名
					logger.info("uploadFile oldFileName ============== > "+oldFileName);
					String prefix=FilenameUtils.getExtension(oldFileName);//原文件后缀     
			        logger.debug("uploadFile prefix============> " + prefix);
					int filesize = 500000;
					logger.debug("uploadFile size============> " + attach.getSize());
			        if(attach.getSize() >  filesize){//上传大小不得超过 500k
		            	request.setAttribute(errorInfo, " * 上传大小不得超过 500k");
		            	flag = false;
		            }else if(prefix.equalsIgnoreCase("jpg") || prefix.equalsIgnoreCase("png") 
		            		|| prefix.equalsIgnoreCase("jpeg") || prefix.equalsIgnoreCase("pneg")){//上传图片格式不正确
		            	String fileName = System.currentTimeMillis()+RandomUtils.nextInt(1000000)+"_Personal.jpg";  
		                logger.debug("new fileName======== " + attach.getName());
		                File targetFile = new File(path, fileName);  
		                if(!targetFile.exists()){  
		                    targetFile.mkdirs();  
		                }  
		                //保存  
		                try {  
		                	attach.transferTo(targetFile);  
		                } catch (Exception e) {  
		                    e.printStackTrace();  
		                    request.setAttribute(errorInfo, " * 上传失败！");
		                    flag = false;
		                }  
		                if(i == 0){
		                	 idPicPath = path+File.separator+fileName;
		                }else if(i == 1){
		                	 workPicPath = path+File.separator+fileName;
		                }
		                logger.debug("idPicPath: " + idPicPath);
		                logger.debug("workPicPath: " + workPicPath);
		                
		            }else{
		            	request.setAttribute(errorInfo, " * 上传图片格式不正确");
		            	flag = false;
		            }
				}
			}
		}
		if(flag){
			user.setModifyBy(((User)session.getAttribute(Constants.USER_SESSION)).getId());
			user.setModifyDate(new Date());
			user.setIdPicPath(idPicPath);
			user.setWorkPicPath(workPicPath);
			try {
				if(userService.modify(user)){
					return "redirect:/sys/user/list.html";
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "usermodify";
	}
	
	@RequestMapping(value="/ucexist.json")
	@ResponseBody
	public Object userCodeIsExit(@RequestParam String userCode){
		logger.debug("userCodeIsExit userCode===================== "+userCode);
		HashMap<String, String> resultMap = new HashMap<String, String>();
		if(StringUtils.isNullOrEmpty(userCode)){
			resultMap.put("userCode", "exist");
		}else{
			User user = null;
			try {
				user = userService.selectUserCodeExist(userCode);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(null != user)
				resultMap.put("userCode", "exist");
			else
				resultMap.put("userCode", "noexist");
		}
		return JSONArray.toJSONString(resultMap);
	}
	
	@RequestMapping(value="/rolelist.json",method=RequestMethod.GET,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public List<Role> getRoleList(){
		List<Role> roleList = null;
		try {
			roleList = roleService.getRoleList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.debug("roleList size: " + roleList.size());
		return roleList;
	}
	
	@RequestMapping(value="/deluser.json",method=RequestMethod.GET)
	@ResponseBody
	public Object deluser(@RequestParam String id){
		HashMap<String, String> resultMap = new HashMap<String, String>();
		if(StringUtils.isNullOrEmpty(id)){
			resultMap.put("delResult", "notexist");
		}else{
			try {
				if(userService.deleteUserById(Integer.parseInt(id)))
					resultMap.put("delResult", "true");
				else
					resultMap.put("delResult", "false");
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return JSONArray.toJSONString(resultMap);
	}
	
	@RequestMapping(value="/view/{id}",method=RequestMethod.GET)
	public String view(@PathVariable String id,Model model,HttpServletRequest request){
		logger.debug("view id===================== "+id);
		User user = new User();
		try {
			user = userService.getUserById(Integer.parseInt(id));
			if(user.getIdPicPath() != null && !"".equals(user.getIdPicPath())){
				String[] paths = user.getIdPicPath().split("\\"+File.separator);
				logger.debug("view picPath paths[paths.length-1]============ " + paths[paths.length-1]);
				user.setIdPicPath(request.getContextPath()+"/statics/uploadfiles/"+paths[paths.length-1]);
			}
			if(user.getWorkPicPath() != null && !"".equals(user.getWorkPicPath())){
				String[] paths = user.getWorkPicPath().split("\\"+File.separator);
				logger.debug("view workPicPath paths[paths.length-1]============ " + paths[paths.length-1]);
				user.setWorkPicPath(request.getContextPath()+"/statics/uploadfiles/"+paths[paths.length-1]);
			}
			
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		model.addAttribute(user);
		return "userview";
	}
	
	@RequestMapping(value="/pwdmodify.html",method=RequestMethod.GET)
	public String pwdModify(HttpSession session){
		if(session.getAttribute(Constants.USER_SESSION) == null){
			return "redirect:/user/login.html";
		}
		return "pwdmodify";
	}
	
	@RequestMapping(value="/pwdmodify.json",method=RequestMethod.POST)
	@ResponseBody
	public Object getPwdByUserId(@RequestParam String oldpassword,HttpSession session){
		logger.debug("getPwdByUserId oldpassword ===================== "+oldpassword);
		HashMap<String, String> resultMap = new HashMap<String, String>();
		if(null == session.getAttribute(Constants.USER_SESSION) ){//session过期
			resultMap.put("result", "sessionerror");
		}else if(StringUtils.isNullOrEmpty(oldpassword)){//旧密码输入为空
			resultMap.put("result", "error");
		}else{
			String sessionPwd = ((User)session.getAttribute(Constants.USER_SESSION)).getUserPassword();
			if(oldpassword.equals(sessionPwd)){
				resultMap.put("result", "true");
			}else{//旧密码输入不正确
				resultMap.put("result", "false");
			}
		}
		return JSONArray.toJSONString(resultMap);
	}
	
	@RequestMapping(value="/pwdsave.html")
	public String pwdSave(@RequestParam(value="newpassword") String newPassword,
							HttpSession session,
							HttpServletRequest request){
		boolean flag = false;
		Object o = session.getAttribute(Constants.USER_SESSION);
		if(o != null && !StringUtils.isNullOrEmpty(newPassword)){
			try {
				flag = userService.updatePwd(((User)o).getId(),newPassword);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(flag){
				request.setAttribute(Constants.SYS_MESSAGE, "修改密码成功,请退出并使用新密码重新登录！");
				session.removeAttribute(Constants.USER_SESSION);//session注销
				return "login";
			}else{
				request.setAttribute(Constants.SYS_MESSAGE, "修改密码失败！");
			}
		}else{
			request.setAttribute(Constants.SYS_MESSAGE, "修改密码失败！");
		}
		return "pwdmodify";
	}
	
}
