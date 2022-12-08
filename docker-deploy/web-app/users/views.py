from django.shortcuts import render, redirect
from .forms import RegisterForm
from django.contrib import auth
from django.contrib import messages
from django.contrib.auth.decorators import login_required
def register(request):
    if request.method == 'POST':
        form = RegisterForm(request.POST)

        if form.is_valid():
            form.save()
            return redirect('login')
    else:
        form = RegisterForm()
    return render(request, 'users/register.html', context={'form': form})
@login_required
def changeEmail(request):
    user = request.user
    context = {}
    context["email"] = user.email
    if request.method == 'POST':
        email = request.POST["email"]
        user.email = email
        user.save()
        # messages.success(request, "change the email successfully")
        return redirect('login')
    else:
        return render(request, 'users/changeEmail.html', context)

# def login(request):
#     if request.method == 'POST':
#         form = LoginForm(request.POST)
#         if form.is_valid():
#             username = form.cleaned_data['username']
#             password = form.cleaned_data['password']
#             user = auth.authenticate(username=username, password=password)
#             if user is not None and user.is_active:
#                 auth.login(request, user)
#                 return HttpResponseRedirect(reverse('amazon_web:dashboard', args=[user.id]))
#             else:
#                 return render(request, 'amazon_web/login.html',
#                               {'form': form, 'message': 'Wrong password. Please try again.'})
#     else:
#         form = LoginForm()

#     return render(request, 'amazon_web/login.html', {'form': form})
