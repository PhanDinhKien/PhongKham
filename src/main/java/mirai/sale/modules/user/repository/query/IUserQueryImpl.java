/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mirai.sale.modules.user.repository.query;

import mirai.sale.modules.user.entity.User;
import static mirai.sale.modules.user.entity.QUser.user;
import static mirai.sale.modules.user.entity.QUserRole.userRole;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
public class  IUserQueryImpl implements IUserQuery{

    private final JPAQueryFactory queryFactory;
    
    public IUserQueryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }
     
    @Override
    public List<User> search(String keyword, Boolean active, String roleCode,Integer page, Integer size) {
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

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        return user.name.containsIgnoreCase(keyword)
                .or(user.email.containsIgnoreCase(keyword));
    }

    private BooleanExpression activeEq(Boolean active) {
        return active == null ? null : user.active.eq(active);
    }

    private BooleanExpression hasRole(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) return null;
        return JPAExpressions.selectOne()                       // (2) subquery
                .from(userRole)
                .where(userRole.user.eq(user),
                       userRole.role.code.eq(roleCode))
                .exists();
    }

    
}