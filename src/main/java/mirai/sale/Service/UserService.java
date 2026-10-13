package mirai.sale.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import mirai.sale.Dto.*;
import mirai.sale.Dto.user.CreateUserRequest;
import mirai.sale.Entity.Role;
import mirai.sale.Entity.User;
import mirai.sale.Entity.UserRole;
import mirai.sale.Exception.BadRequestException;
import mirai.sale.Repository.RoleRepository;
import mirai.sale.Repository.UserRepository;
import mirai.sale.Repository.UserRoleRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository; 
    private final UserRoleRepository userRoleRepository; 
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public UserService(UserRepository userRepository, RoleRepository roleRepository, UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }
   

    @Transactional(readOnly = true)
    public PageResponse<UserListItem> search(UserSearchRequest req) {
        List<UserListItem> items = userRepository.search(req.keyword(),req.active(), req.roleCode(), req.page(), req.size()).stream()
                .map(u -> new UserListItem(u.getId(), u.getName(), u.getEmail(),
                                           u.getActive(), u.getCreatedAt()))
                .toList();
        
        return new PageResponse<>(items, req.page(), req.size(), 100);
    }
    
    @Transactional
    public UserProfileResponse create(CreateUserRequest req){
       Boolean isHasUser = userRepository.findByEmail(req.email()).isPresent(); 
       if(isHasUser) {
         throw new BadRequestException("Tài khoản đã tồn tại");        
       }
       
       List<Role> roles = roleRepository.findByCodeIn(req.roleCode()); 
       
       Set<String> foundCodes = roles.stream().map(Role::getCode).collect(Collectors.toSet());
       
       List<String> missing = req.roleCode().stream().filter(code -> !foundCodes.contains(code)).toList();
       
       if (!missing.isEmpty()) {
            throw new BadRequestException("Vai trò không tồn tại: " + String.join(", ", missing));
       }
       
       User userInsert = new User(); 
       userInsert.setEmail(req.email());
       userInsert.setName(req.name());
       userInsert.setPasswordHash(passwordEncoder.encode(req.password()));
       userInsert.setFailedLoginCount(0);
       userRepository.save(userInsert); 
       
       List<UserRole> userRoles = roles.stream().map(role -> new UserRole(userInsert, role)).toList(); 
       
       List<RoleSummary> roleSummaries = insertRoleOfUser(userInsert, roles); 
      
       return new UserProfileResponse(
                userInsert.getId(), userInsert.getName(), userInsert.getEmail(), userInsert.getActive(),
                roleSummaries,
                Set.of());
    }
    
    
    private List<RoleSummary> insertRoleOfUser(User userInsert, List<Role> roles){
        
        List<UserRole> userRoles = roles.stream().map(role -> new UserRole(userInsert, role)).toList(); 
       
        userRoleRepository.saveAll(userRoles); 
       
        List<RoleSummary> roleSummaries = roles.stream()
                .map(r -> new RoleSummary(r.getId(), r.getCode(), r.getName()))
                .toList();
        return roleSummaries; 
    }
    

}