package mirai.sale.Controller;

import mirai.sale.Dto.PageResponse;
import mirai.sale.Dto.UserListItem;
import mirai.sale.Dto.UserSearchRequest;
import mirai.sale.Service.UserService;
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
}