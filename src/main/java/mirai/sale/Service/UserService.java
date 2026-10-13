package mirai.sale.Service;

import java.util.List;

import mirai.sale.Dto.*;
import mirai.sale.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserListItem> search(UserSearchRequest req) {
        List<UserListItem> items = userRepository.search(req.keyword(),req.active(), req.roleCode(), req.page(), req.size()).stream()
                .map(u -> new UserListItem(u.getId(), u.getName(), u.getEmail(),
                                           u.getActive(), u.getCreatedAt()))
                .toList();
        
        return new PageResponse<>(items, req.page(), req.size(), 100);
    }

}