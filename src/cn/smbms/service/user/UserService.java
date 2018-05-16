package cn.smbms.service.user;
import java.util.List;

import cn.smbms.pojo.User;

public interface UserService {
	
	/**
	 * 用户登录
	 * @param userCode
	 * @param userPassword
	 * @return
	 */
	public User login(String userCode,String userPassword) throws Exception;
	
	/**
	 * 增加用户信息
	 * @param user
	 * @return
	 */
	public boolean add(User user) throws Exception;
	
	/**
	 * 根据条件查询用户列表
	 * @param queryUserName
	 * @param queryUserRole
	 * @return
	 */
	public List<User> getUserList(String queryUserName,Integer queryUserRole,Integer currentPageNo, Integer pageSize) throws Exception;
	/**
	 * 根据条件查询用户表记录数
	 * @param queryUserName
	 * @param queryUserRole
	 * @return
	 */
	public int getUserCount(String queryUserName,Integer queryUserRole) throws Exception;
	
	/**
	 * 根据userCode查询出User
	 * @param userCode
	 * @return
	 */
	public User selectUserCodeExist(String userCode) throws Exception;
	
	/**
	 * 根据ID删除user
	 * @param delId
	 * @return
	 */
	public boolean deleteUserById(Integer delId) throws Exception;
	
	/**
	 * 根据ID查找user
	 * @param id
	 * @return
	 */
	public User getUserById(Integer id) throws Exception;
	
	/**
	 * 修改用户信息
	 * @param user
	 * @return
	 */
	public boolean modify(User user) throws Exception;
	
	/**
	 * 根据userId修改密码
	 * @param id
	 * @param pwd
	 * @return
	 */
	public boolean updatePwd(Integer id,String pwd) throws Exception;
	
}
