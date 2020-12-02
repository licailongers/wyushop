package com.wyu.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.wyu.domain.Order;
import com.wyu.domain.OrderItem;
import com.wyu.domain.Product;
import com.wyu.utils.DataSourceUtils;

public class ProductDao {

	//获取热门商品
	public List<Product> findHotProduct() throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select * from product where is_hot=? limit ?,?";
		return runner.query(sql, new BeanListHandler<Product>(Product.class), 1,0,9);
	}

	//获取最新商品
	public List<Product> findNewProduct() throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select * from product order by pdate desc limit ?,?";
		return runner.query(sql, new BeanListHandler<Product>(Product.class), 0,9);
	}

	//获取当前商品类别的总条数
	public int getTotalCount(String cid) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select count(*) from product where cid=?";
		Long totalCount = (Long) runner.query(sql, new ScalarHandler(),cid);
		return totalCount.intValue();
	}

	//获取当前页面显示数据
	public List<Product> findProductByPage(String cid, int index, int currentCount) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select * from product where cid=? limit ?,?";
		return runner.query(sql, new BeanListHandler<Product>(Product.class), cid,index,currentCount);
	}

	//查找当前商品信息
	public Product findProductByPid(String pid) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select * from product where pid=?";
		return runner.query(sql, new BeanHandler<Product>(Product.class), pid);
	}

	//存储orders表数据
	public void addOrders(Order order) throws SQLException {
		QueryRunner runner = new QueryRunner();
		String sql = "insert into orders values(?,?,?,?,?,?,?,?)";
		Connection conn = DataSourceUtils.getConnection();
		runner.update(conn, sql, order.getOid(),order.getOrdertime(),order.getTotal(),
				order.getState(),order.getAddress(),order.getName(),order.getTelephone(),
				order.getUser().getUid());
	}

	//存储orderitem表数据
	public void addOrderItem(Order order) throws SQLException {
		QueryRunner runner = new QueryRunner();
		String sql = "insert into orderitem values(?,?,?,?,?)";
		Connection conn = DataSourceUtils.getConnection();
		List<OrderItem> orderItems = order.getOrderItems();
		for(OrderItem orderItem : orderItems) {
			runner.update(conn, sql, orderItem.getItemid(),orderItem.getCount(),
					orderItem.getSubtotal(),orderItem.getProduct().getPid(),
					orderItem.getOrder().getOid());		
		}
	}

	//确认订单---更新收货人信息
	public void updateOrderData(Order order) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "update orders set address=?,name=?,telephone=? where oid=?";
		runner.update(sql, order.getAddress(),order.getName(),order.getTelephone(),order.getOid());
	}

	//修改订单状态
	public void updateOrderState(String r6_Order) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "update orders set state=? where oid=?";
		runner.update(sql,1,r6_Order);
	}

	//查询该用户的所有订单信息
	public List<Order> findAllOrders(String uid) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select * from orders where uid=?";
		List<Order> orderList = runner.query(sql, new BeanListHandler<Order>(Order.class), uid);
		return orderList;
	}

	public List<Map<String, Object>> findAllOrderItemByOid(String oid) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select i.count,i.subtotal,p.pimage,p.pname,p.shop_price from orderitem i,product p where i.pid=p.pid and i.oid=?";
		List<Map<String, Object>> mapList = runner.query(sql, new MapListHandler(), oid);
		return mapList;
	}
}
