package cn.smbms.service.role;

import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import cn.smbms.dao.role.RoleMapper;
import cn.smbms.pojo.Role;

@Service
public class RoleServiceImpl implements RoleService{
	@Resource
	private RoleMapper roleMapper;
	
	@Override
	public List<Role> getRoleList() throws Exception{
		// TODO Auto-generated method stub
		return roleMapper.getRoleList();
	}
	
}
