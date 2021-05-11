------------- Delete Script --------------
-- Execute this Script for Fresh DB Setup
delete FROM bill_item_details ;
delete FROM customer_bill_details;
delete FROM cash_counter;
delete FROM customer_details;
delete FROM customer_payment_history;
delete FROM expense_details;
delete FROM opening_stock_value;
delete FROM measurement_units;
delete FROM my_store_details;
delete FROM product_category_details;
delete FROM product_details;
delete FROM product_purchase_price_history;
delete FROM product_stock_ledger;
delete FROM purchase_entry_details;
DELETE FROM PURCHASE_ENTRY_ITEM_DETAILS;
delete FROM sales_return_details;
delete FROM sales_return_items_details;
delete FROM supplier_details where supplier_id not in(1);
delete FROM supplier_payment_history;
delete FROM app_user_details where USER_ID not in(1);
UPDATE APP_USER_DETAILS SET IS_ACTIVE='Y' WHERE USER_TYPE='ADMIN'