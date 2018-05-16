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

import cn.smbms.pojo.Provider;
import cn.smbms.pojo.User;
import cn.smbms.service.provider.ProviderService;
import cn.smbms.tools.Constants;
import cn.smbms.tools.PageSupport;

@Controller
@RequestMapping("/sys/provider")
public class ProviderController extends BaseController{
	
	private Logger logger = Logger.getLogger(ProviderController.class);
	
	@Resource
	private ProviderService providerService;
	
	@RequestMapping(value="/list.html")
	public String getProviderList(Model model,
							@RequestParam(value="queryProCode",required=false) String queryProCode,
							@RequestParam(value="queryProName",required=false) String queryProName,
							@RequestParam(value="pageIndex",required=false) String pageIndex){
		logger.info("getProviderList ---- > queryProCode: " + queryProCode);
		logger.info("getProviderList ---- > queryProName: " + queryProName);
		logger.info("getProviderList ---- > pageIndex: " + pageIndex);
		List<Provider> providerList = null;
		//设置页面容量
    	int pageSize = Constants.pageSize;
    	//当前页码
    	int currentPageNo = 1;
	
		if(queryProCode == null){
			queryProCode = "";
		}
		if(queryProName == null){
			queryProName = "";
		}
    	if(pageIndex != null){
    		try{
    			currentPageNo = Integer.valueOf(pageIndex);
    		}catch(NumberFormatException e){
    			return "redirect:/sys/provider/syserror.html";
    		}
    	}	
    	//总数量（表）	
    	int totalCount = 0;
		try {
			totalCount = providerService.getproviderCount(queryProCode,queryProName);
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
			providerList = providerService.getProviderList(queryProName,queryProCode,currentPageNo, pageSize);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	model.addAttribute("providerList", providerList);
    	model.addAttribute("queryProCode", queryProCode);
		model.addAttribute("queryProName", queryProName);
		model.addAttribute("totalPageCount", totalPageCount);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("currentPageNo", currentPageNo);
		return "providerlist";
	}
	
	@RequestMapping(value="/syserror.html")
	public String sysError(){
		return "syserror";
	}
	
	@RequestMapping(value="/add.html",method=RequestMethod.GET)
	public String add(@ModelAttribute("provider") Provider provider){
		return "provideradd";
	}
	
	@RequestMapping(value="/view/{id}",method=RequestMethod.GET)
	public String view(@PathVariable String id,Model model,HttpServletRequest request){
		logger.debug("view id===================== "+id);
		Provider provider = new Provider();
		try {
			provider = providerService.getProviderById(Integer.parseInt(id));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(provider.getCompanyLicPicPath() != null && !"".equals(provider.getCompanyLicPicPath())){
			String[] paths = provider.getCompanyLicPicPath().split("\\"+File.separator);
			logger.debug("view companyLicPicPath paths[paths.length-1]============ " + paths[paths.length-1]);
			provider.setCompanyLicPicPath(request.getContextPath()+"/statics/uploadfiles/"+paths[paths.length-1]);
		}
		if(provider.getOrgCodePicPath() != null && !"".equals(provider.getOrgCodePicPath())){
			String[] paths = provider.getOrgCodePicPath().split("\\"+File.separator);
			logger.debug("view orgCodePicPath paths[paths.length-1]============ " + paths[paths.length-1]);
			provider.setOrgCodePicPath(request.getContextPath()+"/statics/uploadfiles/"+paths[paths.length-1]);
		}

		model.addAttribute(provider);
		return "providerview";
	}
	
	@RequestMapping(value="/modify/{id}",method=RequestMethod.GET)
	public String getProviderById(@PathVariable String id,Model model,HttpServletRequest request){
		logger.debug("getProviderById id===================== "+id);
		Provider provider = new Provider();
		try {
			provider = providerService.getProviderById(Integer.parseInt(id));
			if(provider.getCompanyLicPicPath() != null && !"".equals(provider.getCompanyLicPicPath())){
				String[] paths = provider.getCompanyLicPicPath().split("\\"+File.separator);
				logger.debug("view companyLicPicPath paths[paths.length-1]============ " + paths[paths.length-1]);
				provider.setCompanyLicPicPath(request.getContextPath()+"/statics/uploadfiles/"+paths[paths.length-1]);
			}
			if(provider.getOrgCodePicPath() != null && !"".equals(provider.getOrgCodePicPath())){
				String[] paths = provider.getOrgCodePicPath().split("\\"+File.separator);
				logger.debug("view orgCodePicPath paths[paths.length-1]============ " + paths[paths.length-1]);
				provider.setOrgCodePicPath(request.getContextPath()+"/statics/uploadfiles/"+paths[paths.length-1]);
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		model.addAttribute(provider);
		return "providermodify";
	}
	
	@RequestMapping(value="/modifysave.html",method=RequestMethod.POST)
	public String modifyProviderSave(Provider provider,HttpSession session,HttpServletRequest request,
									@RequestParam(value ="attachs", required = false) MultipartFile[] attachs){
		logger.debug("modifyProviderSave id===================== "+provider.getId());
		String companyLicPicPath = null;
		String orgCodePicPath = null;
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
						errorInfo = "uploadOcError";
		        	}
					String oldFileName = attach.getOriginalFilename();//原文件名
					String prefix=FilenameUtils.getExtension(oldFileName);//原文件后缀     
					int filesize = 500000;
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
		                	companyLicPicPath = path+File.separator+fileName;
		                }else if(i == 1){
		                	orgCodePicPath = path+File.separator+fileName;
		                }
		                logger.debug("companyLicPicPath: " + companyLicPicPath);
		                logger.debug("orgCodePicPath: " + orgCodePicPath);
		                
		            }else{
		            	request.setAttribute(errorInfo, " * 上传图片格式不正确");
		            	flag = false;
		            }
				}
			}
		}
		if(flag){
			provider.setModifyBy(((User)session.getAttribute(Constants.USER_SESSION)).getId());
			provider.setModifyDate(new Date());
			provider.setCompanyLicPicPath(companyLicPicPath);
			provider.setOrgCodePicPath(orgCodePicPath);
			try {
				if(providerService.modify(provider)){
					return "redirect:/sys/provider/list.html";
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "providermodify";
	}
	
	@RequestMapping(value="/addsave.html",method=RequestMethod.POST)
	public String addProviderSave(Provider provider,HttpSession session,HttpServletRequest request,
			 			@RequestParam(value ="attachs", required = false) MultipartFile[] attachs){
		String companyLicPicPath = null;
		String orgCodePicPath = null;
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
					errorInfo = "uploadOcError";
	        	}
				String oldFileName = attach.getOriginalFilename();//原文件名
				String prefix=FilenameUtils.getExtension(oldFileName);//原文件后缀     
				int filesize = 500000;
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
	                	companyLicPicPath = path+File.separator+fileName;
	                }else if(i == 1){
	                	orgCodePicPath = path+File.separator+fileName;
	                }
	                logger.debug("companyLicPicPath: " + companyLicPicPath);
	                logger.debug("orgCodePicPath: " + orgCodePicPath);
	                
	            }else{
	            	request.setAttribute(errorInfo, " * 上传图片格式不正确");
	            	flag = false;
	            }
			}
		}
		if(flag){
			provider.setCreatedBy(((User)session.getAttribute(Constants.USER_SESSION)).getId());
			provider.setCreationDate(new Date());
			provider.setCompanyLicPicPath(companyLicPicPath);
			provider.setOrgCodePicPath(orgCodePicPath);
			try {
				if(providerService.add(provider)){
					return "redirect:/sys/provider/list.html";
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "provideradd";
	}
	
	@RequestMapping(value="/del.json",method=RequestMethod.POST)
	@ResponseBody
	public Object delProviderById(@RequestParam(value="proid") String id){
		logger.debug("delProviderById proid ===================== "+id);
		HashMap<String, String> resultMap = new HashMap<String, String>();
		if(!StringUtils.isNullOrEmpty(id)){
			boolean flag = false;
			try {
				flag = providerService.smbmsdeleteProviderById(Integer.parseInt(id));
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(flag){//删除成功
				resultMap.put("delResult", "true");
			}else{//删除失败
				resultMap.put("delResult", "false");
			}
		}else{
			resultMap.put("delResult", "notexit");
		}
		return JSONArray.toJSONString(resultMap);
	}
}
