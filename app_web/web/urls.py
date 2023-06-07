from django.urls import path
from web import views as web_views
from web import ajax as web_ajax

urlpatterns = [
    #url genéricos
    path('', web_views.main_page, name='web'),
    path('grupo/', web_views.group_page, name='grupo'),
    path('reservar/', web_views.reservation_page, name='reservar'),
    path('registro/', web_views.registration_page, name='registro'),

    #acciones admin
    path('listar_musicos_admin/', web_ajax.listar_musicos_admin, name='listar_musicos_admin'),
    path('get_tabla_registro_admin/', web_ajax.get_tabla_registro_admin, name='get_tabla_registro_admin'),

    #acciones grupo
    path('listar_musicos/', web_ajax.listar_musicos, name='listar_musicos'),
    path('crear_musico_view/', web_views.crear_musico_view, name='crear_musico_view'),
    path('editar_musico_view/', web_views.editar_musico_view, name='editar_musico_view'), 
    path('crear_musico_action/', web_ajax.crear_musico_action, name='crear_musico_action'),
    path('editar_musico_action/', web_ajax.editar_musico_action, name='editar_musico_action'),
    path('borrar_musico_action/', web_ajax.borrar_musico_action, name='borrar_musico_action'),

    #acciones reservar
    path('get_horario/', web_ajax.get_horario, name='get_horario'),
    path('reservar_action/', web_ajax.reservar_action, name='reservar_action'),

    #acciones registro  
    path('get_tabla_registro/', web_ajax.get_tabla_registro, name='get_tabla_registro'),
    path('borrar_registro_action/', web_ajax.borrar_registro_action, name='borrar_registro_action'),
    path('editar_registro_view/', web_views.editar_registro_view, name='editar_registro_view'),
    path('editar_registro_action/', web_ajax.editar_registro_action, name='editar_registro_action'),

]