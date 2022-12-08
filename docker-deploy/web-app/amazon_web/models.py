from django.db import models
from django.contrib.auth.models import User
# Create your models here.
class Warehouse(models.Model):
    whid = models.IntegerField(primary_key=True)
    wh_x = models.IntegerField()
    wh_y = models.IntegerField()

class Product(models.Model):
    name = models.CharField(max_length=256)
    description = models.CharField(max_length=256)
    amounts = models.IntegerField()
    price = models.FloatField(default=1)
    wh =models.ForeignKey(Warehouse, on_delete=models.CASCADE, null=True)
    rate = models.FloatField(default=5)
    people_rate = models.IntegerField(default=1)
    img = models.CharField(max_length=300, null=True)

class Order(models.Model):
    packege_id = models.AutoField(primary_key=True)
    owner = models.ForeignKey(User, on_delete=models.CASCADE, null=True)
    product = models.ForeignKey(Product, on_delete=models.SET_NULL, null=True)
    amounts = models.IntegerField()
    price = models.FloatField(default=1)
    truck_id = models.IntegerField(blank=True, null=True)
    #if this order is in cart
    status = models.CharField(max_length=20, default ="cart")
    destination_x = models.IntegerField(null = True)
    destination_y = models.IntegerField(null = True)
    wh =models.ForeignKey(Warehouse, on_delete=models.CASCADE, null=True)
    ups = models.CharField(max_length=50, blank=True)
    # order_type = models.CharField(max_length=10, default ="normal")