/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mirai.sale.seeder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import mirai.sale.modules.role.entity.Permission;
import mirai.sale.modules.role.repository.PermissionRepository;
import mirai.sale.modules.role.repository.RolePermissionRepository;
import mirai.sale.modules.role.repository.RoleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;
import java.util.function.Function;
import mirai.sale.modules.role.constant.PermissionCode;
import mirai.sale.modules.role.entity.Role;
import mirai.sale.modules.role.entity.RolePermission;
/**
 *
 * @author macos
 */
@Component
@Order(2)
public class PermissionSeeder implements ApplicationRunner {

    private final static String ADMIN_ROLE_CODE = "ADMIN"; 
    
    private final RoleRepository roleRepository; 
    private final PermissionRepository permissionRepository; 
    private final RolePermissionRepository rolePermissionRepository; 

    public PermissionSeeder(RoleRepository roleRepository, PermissionRepository permissionRepository, RolePermissionRepository rolePermissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
      List<Permission> p =  insertPermission(); 
      updatePermissionRoleAdmin(p); 
      
    }
    
    private List<Permission> insertPermission() {
       
       Map<String, Permission> mapByKey = this.permissionRepository.findAll().stream().collect(Collectors.toMap(Permission::getCode, Function.identity())); 
       
       List<Permission> changed = new ArrayList<>();       
       
       for(PermissionCode code : PermissionCode.values()){
           
          Permission p = mapByKey.get(code.name()); 
          
          if(p == null){
             p = new Permission(); 
             p.setCode(code.name());

             mapByKey.put(code.name(), p);
          }
          else if(Objects.equals(p.getName(), code.getLabel()) && Objects.equals(p.getModule(), code.getModule())){
             continue;
          }
          
          p.setModule(code.getModule());  
          p.setName(code.getLabel());      
          changed.add(p); 
       }
       
        if (!changed.isEmpty()) {
            permissionRepository.saveAll(changed);
        }
        return List.copyOf(mapByKey.values());
    } 
    
    private void updatePermissionRoleAdmin( List<Permission> listPermissions){
       
        Role role  = roleRepository.findByCode(ADMIN_ROLE_CODE).orElse(null); 
        
        if(role != null) {
           Set<UUID> ids = rolePermissionRepository.findPermissionIdByRole(role); 
           
           List<RolePermission> permissions =  listPermissions.stream()
                   .filter(p -> !ids.contains(p.getId()))
                   .map(mapper -> new RolePermission(role, mapper)).toList(); 
           
           if(!permissions.isEmpty()){
             rolePermissionRepository.saveAll(permissions); 
           }
        }
    
    }
    
}
