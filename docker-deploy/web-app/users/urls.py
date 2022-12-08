
from django.conf.urls import url
from . import views
from django.urls import path

app_name = 'users'
urlpatterns = [
    url(r'^register/', views.register, name='register'),
    path('changeEmail', views.changeEmail, name="changeEmail"),
]