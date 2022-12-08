from django.http import HttpResponse, JsonResponse, HttpResponseRedirect
from django.shortcuts import get_object_or_404,render,redirect
from django.views import View
from django.contrib.auth.decorators import login_required
from .models import *
from django.db.models import Q
from django.contrib import messages
from django.core.mail import send_mail
from django.conf import settings
from . import front_end_pb2 as front
from .utils import connect_backend, send_request
# Create your views here.

@login_required
def listAllProducts(request):
    context={}
    products = Product.objects.all()
    if request.method == "POST":
        search = request.POST["search"]
        # converted = search.str.lower()
        converted = search.lower()

        myproducts = []
        for product in products:
            name = product.name.lower()
            description = product.description.lower()
            if name.find(converted)!=-1 or description.find(converted)!=-1:
                myproducts.append(product)
        context = {'products':myproducts}
        return render(request, 'listAllProducts.html', context) 
    else:
        context = {'products':products}
        return render(request, 'listAllProducts.html', context) 

@login_required
def product_detail(request, product_id):
    product = get_object_or_404(Product, pk=product_id)
    context={}
    context["product"] = product
    if request.method == "POST":
        if request.POST["action"] == "buy":
            amount = request.POST["amount"]
            # if product.amounts < amount:
            #     messages.error(request, 'product isn\'t sufficient, please try again later')
            # dest_x = request.POST["dest_x"]
            # dest_y = request.POST["dest_y"]
            # ups = request.POST["ups_account"]
            #check if there's enough products
            # order = Order(owner=request.user, product=product, amounts=amount,price=product.price,
            # destination_x=dest_x,destination_y=dest_y,wh = product.wh,ups = ups,status="open")
            order = Order(owner=request.user, product=product, amounts=amount,price=product.price,
            wh = product.wh,status="open")
            order.save()
            # context["order"] = order
            return redirect('purchase', order.packege_id) 
        elif request.POST["action"] == "add_to_cart":
            amount = request.POST["amount"]
            order = Order(owner=request.user, product=product, amounts=amount,price=product.price,
            wh = product.wh,status="cart")
            order.save()
            context["order"] = order
            return redirect('cart_detail')
        else:
            curr_rate = request.POST["rate"]
            rate = (float(curr_rate)*product.people_rate+product.rate)/(product.people_rate+1)
            product.rate = int(rate * 100)/100
            product.people_rate = product.people_rate + 1
            product.save()
            return render(request, 'product_detail.html', context) 
    else:
        return render(request, 'product_detail.html', context) 
#fill in payment information and check out
@login_required
def purchase(request, packege_id):
    order = get_object_or_404(Order, pk=packege_id)
    context={}
    if request.method == "POST":
        order.destination_x = request.POST["dest_x"]
        order.destination_y = request.POST["dest_y"]
        order.ups = request.POST["ups_account"]
        #check if there's enough products
        order.save()
        #################发送给后台&发送邮件
        # command = front_end_pd2.FECommands()
        command = front.FECommands()
        command.purchase.packege_id = packege_id
        s = connect_backend(('amazon', 11111))
        send_request(command,s)
        send_mail(
            'Purchase success',
            f'Your order {order.packege_id} is processing. You purchased {order.amounts} product {order.product.name}.',
            from_email=settings.EMAIL_HOST_USER,
            recipient_list=[request.user.email],
            fail_silently=False,
        )
        context["order"] = order
        return render(request, 'order_detail.html', context) 
    else: 
        context["order"] = order
        return render(request, 'purchase.html', context)

#show details of shopping cart if method is post, orders in cart will be checked out
@login_required
def cart_detail(request):
    orders = Order.objects.all().filter(Q(owner = request.user)&Q(status="cart"))
    context={}
    if request.method == "POST":
        for order in orders:
            order.destination_x = request.POST["dest_x"]
            order.destination_y = request.POST["dest_y"]
            order.ups = request.POST["ups_account"]
            order.status = "open"
            order.save()
            #################发送给后台&发送邮件
            command = front.FECommands()
            command.purchase.packege_id = order.packege_id
            s = connect_backend(('amazon', 11111))
            send_request(command,s)
            send_mail(
                'Purchase success',
                f'Your order {order.packege_id} is processing. You purchased {order.amounts} product {order.product.name}.',
                from_email=settings.EMAIL_HOST_USER,
                recipient_list=[request.user.email],
                fail_silently=False,
            )
        context["orders"] = orders
        context["bought"] = True
        return render(request,'cart_detail.html',context) 
    else:
        context["orders"] = orders
        context["bought"] = False
        return render(request,'cart_detail.html',context) 
        
@login_required
def cancelOrder(request,packege_id):
     order = Order.objects.get(pk=packege_id)
     order.status = "cancel"
     order.save()
     return redirect('cart_detail')



@login_required
def query(request):
    context={}
    if request.method == "POST":
        packege_id = request.POST["packege_id"]
        #if order id doesn't exist??????
        # order = get_object_or_404(Order, pk=packege_id)
        try:
            order = Order.objects.get(pk=packege_id)
        except Order.DoesNotExist:
            messages.error(request, 'Order you queried does not exist, please try again')
            return render(request,'query.html')
        context["order"] = order
        return render(request,'order_detail.html',context)
    else:
        return render(request,'query.html')

@login_required
def order_list(request):
    orders = Order.objects.all().filter(owner = request.user)
    context={'orders':orders}
    return render(request,'order_list.html',context)
@login_required
def clear_cart(request):
    orders = Order.objects.all().filter(Q(owner = request.user)&Q(status="cart"))
    for order in orders:
        order.delete()
    return redirect('cart_detail')
