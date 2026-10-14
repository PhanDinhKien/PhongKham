/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mirai.sale.modules.user.repository.query;

import mirai.sale.modules.user.entity.User;
import java.util.List;
public interface  IUserQuery {
      public List<User> search(String keyword, Boolean active, String roleCode, Integer page, Integer size);
}