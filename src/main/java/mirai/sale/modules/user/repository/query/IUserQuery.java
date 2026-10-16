package mirai.sale.modules.user.repository.query;

import mirai.sale.modules.user.entity.User;
import java.util.List;

public interface IUserQuery {
    List<User> search(String keyword, Boolean active, String roleCode, Integer page, Integer size);
    long countSearch(String keyword, Boolean active, String roleCode);
}