package com.wyu.web.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;

import com.wyu.domain.Order;
import com.wyu.domain.OrderItem;
import com.wyu.domain.Product;
import com.wyu.domain.User;
import com.wyu.service.ProductService;

public class MyOrdersServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		
		//判断用户是否已经登录---使用了过滤器Filter判断，所以不需要再判断
		User user = (User) session.getAttribute("user");
		/*
		   if(user==null) {
		 
			response.sendRedirect(request.getContextPath()+"/login.jsp");
			return;
			}
		*/
		
		//查询该用户的所有订单信息
		ProductService service = new ProductService();
		List<Order> orderList = service.findAllOrders(user.getUid());
		//循环所有的订单为每个订单填充订单项集合信息
		if(orderList!=null) {
			for(Order order : orderList) {
				//获得每一个订单的oid
				String oid = order.getOid();
				//查询该订单的所有订单项
				List<Map<String, Object>> mapList = service.findAllOrderItemByOid(oid);
				//将mapList转换成List<OrderItem> orderItems
				for(Map<String, Object> map : mapList) {
					try {
						//从map中取出count、subtotal封装到OrderItem中
						OrderItem orderItem = new OrderItem();
						BeanUtils.populate(orderItem, map);
						//从map中取出pimage、pname、shop_price封装到Product中
						Product product = new Product();
						BeanUtils.populate(product, map);
						//将product封装到OrderItem
						orderItem.setProduct(product);
						//将orderitem封装到order中的orderitems中
						order.getOrderItems().add(orderItem);
					} catch (IllegalAccessException | InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
		}
		//orderList封装完成
		request.setAttribute("orderList", orderList);
		request.getRequestDispatcher("/order_list.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
