package mirai.sale.modules.user.controller;

import mirai.sale.common.dto.PageResponse;
import mirai.sale.modules.user.dto.CreateUserRequest;
import mirai.sale.modules.user.dto.UpdateUserRequest;
import mirai.sale.modules.user.dto.UserListItem;
import mirai.sale.modules.user.dto.UserProfileResponse;
import mirai.sale.modules.user.dto.UserSearchRequest;
import mirai.sale.modules.user.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/users?keyword=admin&active=true&roleCode=ADMIN&page=0&size=10
    @GetMapping
    public PageResponse<UserListItem> search(UserSearchRequest req) {
        return userService.search(req);
    }
    
    @PostMapping
    public UserProfileResponse insert(@Valid @RequestBody CreateUserRequest data) 
    {
        return userService.create(data); 
    }
    
    @PutMapping("/{id}")
    public UserProfileResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest data) 
    {
        return userService.update(data, id); 
    }
}