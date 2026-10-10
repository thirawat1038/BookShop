package app;

// รวมระบบหนังสือ สมาชิก การขาย และแอดมิน

import repository.BookFile;
import repository.MemberFile;
import repository.OrderFile;
import service.AdminAccounts;
import service.AdminBookManager;
import service.BookManager;
import service.LoginManager;
import service.MemberManager;
import service.OrderManager;
import service.PurchaseManager;

public final class ShopSystem {
    public final BookManager products;
    public final OrderManager orders;
    public final MemberManager members;
    public final PurchaseManager checkout;
    public final AdminAccounts adminAuth;
    public final LoginManager login;
    public final AdminBookManager inventory;

    public ShopSystem(DataFiles paths) {
        BookFile productRepository = new BookFile(paths.products().toString());
        products = new BookManager(productRepository);
        orders = new OrderManager(products, new OrderFile(paths.orders().toString(), productRepository));
        members = new MemberManager(new MemberFile(paths.members().toString()));
        checkout = new PurchaseManager(products, orders, paths.products(), paths.orders());
        adminAuth = new AdminAccounts(paths.adminAccount());
        login = new LoginManager(members, adminAuth);
        inventory = new AdminBookManager(adminAuth, products, checkout, orders);
    }
}
