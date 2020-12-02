package com.wyu.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.wyu.dao.ProductDao;
import com.wyu.domain.Order;
import com.wyu.domain.OrderItem;
import com.wyu.domain.PageBean;
import com.wyu.domain.Product;
import com.wyu.utils.DataSourceUtils;

public class ProductService {

	//获取热门商品
	public List<Product> findHotProduct() {
		ProductDao dao = new ProductDao();
		List<Product> hotProductList = null;
		try {
			hotProductList = dao.findHotProduct();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return hotProductList;
	}

	//获取最新商品
	public List<Product> findNewProduct() {
		ProductDao dao = new ProductDao();
		List<Product> newProductList = null;
		try {
			newProductList = dao.findNewProduct();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return newProductList;
	}

	//封装一个PageBean
	public PageBean<Product> findCategroyByCid(String cid,int currentPage,int currentCount) {
		ProductDao dao = new ProductDao();
		//封装一个pageBean返回给web层
		PageBean<Product> pageBean = new PageBean<Product>();

		//1、封装当前页
		pageBean.setCurrentPage(currentPage);
		//2、封装当前条数
		pageBean.setCurrentCount(currentCount);
		//3、封装总条数
		int totalCount = 0;
		try {
			totalCount = dao.getTotalCount(cid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pageBean.setTotalCount(totalCount);
		//4、封装总页数
		int totalPage = (int)Math.ceil(1.0*totalCount/currentCount);
		pageBean.setTotalPage(totalPage);
		//5、当前显示的数据
		int index = (currentPage-1)*currentCount;
		List<Product> list = null;
		try {
			list = dao.findProductByPage(cid,index,currentCount);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		pageBean.setList(list);
		
		return pageBean;
	}

	//查找商品信息
	public Product findProductByPid(String pid) {
		ProductDao dao = new ProductDao();
		Product product = null;
		try {
			product = dao.findProductByPid(pid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return product;
	}

	//提交订单
	public void submitOrder(Order order) {
		ProductDao dao = new ProductDao();
		try {
			//开启事务
			DataSourceUtils.startTransaction();
			//存储orders表数据的方法
			dao.addOrders(order);
			//存储orderitem表数据的方法
			dao.addOrderItem(order);
		} catch (SQLException e) {
			try {
				DataSourceUtils.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}	finally {
			try {
				DataSourceUtils.commitAndRelease();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	//更新收货人信息
	public void updateOrderData(Order order) {
		ProductDao dao = new ProductDao();
		try {
			dao.updateOrderData(order);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	//修改订单支付状态
	public void updateOrderState(String r6_Order) {
		ProductDao dao = new ProductDao();
		try {
			dao.updateOrderState(r6_Order);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<Order> findAllOrders(String uid) {
		ProductDao dao = new ProductDao();
		List<Order> orderList = null;
		try {
			orderList = dao.findAllOrders(uid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return orderList;
	}

	public List<Map<String, Object>> findAllOrderItemByOid(String oid) {
		ProductDao dao = new ProductDao();
		List<Map<String, Object>> mapList = null;
		try {
			mapList = dao.findAllOrderItemByOid(oid);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return mapList;
	}
}
