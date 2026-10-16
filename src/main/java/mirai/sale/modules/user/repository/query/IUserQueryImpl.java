package mirai.sale.modules.user.repository.query;

import mirai.sale.modules.user.entity.User;
import static mirai.sale.modules.user.entity.QUser.user;
import static mirai.sale.modules.user.entity.QUserRole.userRole;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;

public class IUserQueryImpl implements IUserQuery {

    private final JPAQueryFactory queryFactory;
    
    public IUserQueryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }
     
    @Override
    public List<User> search(String keyword, Boolean active, String roleCode, Integer page, Integer size) {
        return queryFactory
                .selectFrom(user)
                .where(
                        keywordContains(keyword),
                        activeEq(active),
                        hasRole(roleCode)
                )
                .orderBy(user.createdAt.desc())
                .offset((long) page * size)
                .limit(size)
                .fetch();
    }

    @Override
    public long countSearch(String keyword, Boolean active, String roleCode) {
        Long count = queryFactory
                .select(user.count())
                .from(user)
                .where(
                        keywordContains(keyword),
                        activeEq(active),
                        hasRole(roleCode)
                )
                .fetchOne();
        return count != null ? count : 0L;
    }

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        String kw = keyword.trim();
        BooleanExpression exp = user.name.containsIgnoreCase(kw)
                .or(user.email.containsIgnoreCase(kw));
        if (user.staffCode != null) {
            exp = exp.or(user.staffCode.containsIgnoreCase(kw));
        }
        if (user.phone != null) {
            exp = exp.or(user.phone.containsIgnoreCase(kw));
        }
        return exp;
    }

    private BooleanExpression activeEq(Boolean active) {
        return active == null ? null : user.active.eq(active);
    }

    private BooleanExpression hasRole(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) return null;
        return JPAExpressions.selectOne()
                .from(userRole)
                .where(userRole.user.eq(user),
                       userRole.role.code.eq(roleCode))
                .exists();
    }
}