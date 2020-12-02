package com.wyu.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import com.wyu.domain.Category;
import com.wyu.domain.Order;
import com.wyu.domain.Product;
import com.wyu.utils.DataSourceUtils;

public class AdminDao {

	public List<Category> findAllCategory() throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select * from category";
		return runner.query(sql, new BeanListHandler<Category>(Category.class));
	}

	public void saveProduct(Product product) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "insert into product values(?,?,?,?,?,?,?,?,?,?)";
		runner.update(sql, product.getPid(),product.getPname(),product.getMarket_price(),
				product.getShop_price(),product.getPimage(),product.getPdate(),
				product.getIs_hot(),product.getPdesc(),product.getPflag(),product.getCategory().getCid());
	}

	public List<Order> findAllOrders() throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select * from orders";
		return runner.query(sql, new BeanListHandler<Order>(Order.class));
	}

	public List<Map<String, Object>> findOrderInfoByOid(String oid) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select p.pimage,p.pname,p.shop_price,i.count,i.subtotal "+
					" from orderitem i,product p "+
					" where i.pid=p.pid and i.oid=? ";
		return runner.query(sql, new MapListHandler(), oid);
	}

	public List<Product> findAllProduct() throws SQLException{
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select * from product";
		List<Product> productList = runner.query(sql, new BeanListHandler<Product>(Product.class));
		return productList;
	}


	public void addProduct(Product product) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "insert into product values(?,?,?,?,?,?,?,?,?,?)";
		runner.update(sql, product.getPid(),product.getPname(),product.getMarket_price(),
					product.getShop_price(),product.getPimage(),product.getPdate(),
					product.getIs_hot(),product.getPdesc(),product.getPflag(),product.getCategory().getCid());
	}

	public void delProductById(String pid) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "delete from product where pid=?";
		runner.update(sql, pid);
	}

	public Product findProductById(String pid) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select * from product where pid=?";
		Product product = runner.query(sql, new BeanHandler<Product>(Product.class), pid);
		return product;
	}

	public void updateProduct(Product product) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "update product set pname=?,market_price=?,shop_price=?,pimage=?,pdate=?,is_hot=?,pdesc=?,pflag=?,cid=? where pid=?";
		runner.update(sql, product.getPname(),product.getMarket_price(),product.getShop_price(),
					product.getPimage(),product.getPdate(),product.getIs_hot(),product.getPdesc(),
					product.getPflag(),product.getCategory().getCid(),product.getPid());
	}

	// 添加商品类别
	public void addCategory(Category category) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "insert into category values(?,?)";
		runner.update(sql, category.getCid(),category.getCname());
	}

	// 根据商品类别ID删除该商品类别
	public void delCategoryByCid(String cid) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "delete from category where cid=?";
		runner.update(sql, cid);
	}

	// 根据cid查找category
	public Category findCategoryByCid(String cid) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select * from category where cid=?";
		return runner.query(sql, new BeanHandler<Category>(Category.class),cid);
	}

	// 编辑Category
	public void eidtCategory(String cid, String cname) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "update category set cname=? where cid=?";
		runner.update(sql, cname,cid);
	}
}
