package lk.ijse.dep.pos.business.custom.impl;

import lk.ijse.dep.pos.business.custom.OrderBO;
import lk.ijse.dep.pos.dao.custom.ItemDAO;
import lk.ijse.dep.pos.dao.custom.OrderDAO;
import lk.ijse.dep.pos.dao.custom.OrderDetailDAO;
import lk.ijse.dep.pos.dao.custom.QueryDAO;
import lk.ijse.dep.pos.db.JPAUtil;
import lk.ijse.dep.pos.dto.OrderDTO;
import lk.ijse.dep.pos.dto.OrderDTO2;
import lk.ijse.dep.pos.dto.OrderDetailDTO;
import lk.ijse.dep.pos.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class OrderBOImpl implements OrderBO {

    @Autowired
    private OrderDAO orderDAO;
    @Autowired
    private OrderDetailDAO orderDetailDAO;
    @Autowired
    private ItemDAO itemDAO;
    @Autowired
    private QueryDAO queryDAO;

    @Override
    public int getLastOrderId() throws Exception {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        orderDAO.setEntityManager(em);
        em.getTransaction().begin();
        int lastOrderId = orderDAO.getLastOrderId();
        em.getTransaction().commit();
        em.close();
        return lastOrderId;
    }

    @Override
    public void placeOrder(OrderDTO order) throws Exception {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        orderDAO.setEntityManager(em);
        itemDAO.setEntityManager(em);
        orderDetailDAO.setEntityManager(em);
        em.getTransaction().begin();
        int oId = order.getId();
        orderDAO.save(new Order(oId, new java.sql.Date(new Date().getTime()),
                em.getReference(Customer.class, order.getCustomerId())));
        for (OrderDetailDTO orderDetail : order.getOrderDetails()) {
            orderDetailDAO.save(new OrderDetail(oId, orderDetail.getCode(),
                    orderDetail.getQty(), orderDetail.getUnitPrice()));
            Item item = itemDAO.find(orderDetail.getCode());
            item.setQtyOnHand(item.getQtyOnHand() - orderDetail.getQty());
            itemDAO.update(item);
        }
        em.getTransaction().commit();
        em.close();
    }

    @Override
    public List<OrderDTO2> getOrderInfo(String query) throws Exception {
        return null;
    }

    @Override
    public List<OrderDTO2> getOrderInfo2(String query) throws Exception {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        queryDAO.setEntityManager(em);
        em.getTransaction().begin();

        List<CustomEntity> ordersInfo = queryDAO.getOrdersInfo(query + "%");
        em.getTransaction().commit();
        em.close();

        List<OrderDTO2> dtos = new ArrayList<>();
        for (CustomEntity info : ordersInfo) {
            dtos.add(new OrderDTO2(info.getOrderId(),
                    new java.sql.Date(info.getOrderDate().getTime()), info.getCustomerId(), info.getCustomerName(), info.getOrderTotal()));
        }
        return dtos;
    }


}
