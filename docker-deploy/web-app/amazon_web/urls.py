from django.urls import path

from . import views

#html url views 要如何对应
urlpatterns = [
    path('', views.listAllProducts, name="listAllProducts"),

    path('product/<int:product_id>', views.product_detail, name="product_detail"),

    path('order/<int:packege_id>', views.purchase, name="purchase"),
    path('order_list', views.order_list, name="order_list"),  
    path('clear_cart', views.clear_cart, name="clear_cart"),    

    path('cart_detail', views.cart_detail, name="cart_detail"),
    path('cancelOrder/<int:packege_id>', views.cancelOrder, name="cancelOrder"),
    path('query', views.query, name="query"),

]
