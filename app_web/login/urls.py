from django.urls import path
from login import ajax as login_ajax
from login import views as login_views


urlpatterns = [
    path('', login_views.login, name='login'), 
    path('sign_up/', login_views.sign_up, name='sign_up'),
    path('auth_log/', login_views.auth_log, name='auth_log'),
    path('sign_up_action/', login_views.sign_up_action, name='sign_up_action'),
    
]