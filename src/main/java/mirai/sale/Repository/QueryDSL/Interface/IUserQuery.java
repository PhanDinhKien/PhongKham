/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mirai.sale.Repository.QueryDSL.Interface;

import java.util.List;
import mirai.sale.Entity.User;


public interface  IUserQuery {
      public List<User> search(String keyword, Boolean active, String roleCode, Integer page, Integer size);
}