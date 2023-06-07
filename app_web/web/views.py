from django.shortcuts import render, redirect
from lasSalas.connection import open_database


# Create your views here.
def main_page(request):
    #recoger el usuario y retornar al login si no lo tiene almacenado
    username = request.session.get('username')
    if not username:
        return redirect('login')
    is_admin = False if username != "admin" else True
    template_name = 'main.html'
    return render(request, template_name, {'is_admin': is_admin})

def group_page(request):
    #recoger el usuario y retornar al login si no lo tiene almacenado
    username = request.session.get('username')
    if not username:
        return redirect('login')
    #dar true/false en función de si es administrador
    is_admin = False if username != "admin" else True
    template_name = 'grupo/grupo.html'
    return render(request, template_name, {'is_admin': is_admin})

def crear_musico_view(request):
    template_name = 'grupo/crear_musico.html'
    return render(request, template_name)

def editar_musico_view(request):
    #recoger la bbdd
    database = open_database()
    # recoger el grupo de sesión y el id del músico de request
    grupo = request.session['username']
    musico_id = request.GET.get('musico_id')
    # si el usuario es administrador el grupo también se recoge del request
    if request.session['username'] == "admin":
        grupo = request.GET.get('grupo')
    # llamada a la bbdd al nodo de Grupos, con el grupo y el id del músico
    # y guardar los datos en una variable que devolver con render
    music_data = database.child("Grupos").child(grupo).child(musico_id).get().val()
    template_name = 'grupo/editar_musico.html'
    return render(request, template_name, {'music_data': music_data})

def reservation_page(request):
    if not request.session.get('username'):
        return redirect('login')
    template_name = 'reservar/reservar.html'
    return render(request, template_name)

def registration_page(request):
    #recoger el usuario y retornar al login si no lo tiene almacenado
    username = request.session.get('username')
    if not username:
        return redirect('login')
    #dar true/false en función de si es administrador
    is_admin = False if username != "admin" else True
    template_name = 'registro/registro.html'
    return render(request, template_name, {'is_admin': is_admin})

def editar_registro_view(request):
    # llamada a bbdd
    database = open_database()
    # recoger variables
    grupo = request.session['username']
    parameters = request.GET
    if request.session['username'] == "admin":
        grupo = parameters.get('grupo')
    registro_date = parameters.get('registro_date')
    sala = parameters.get('sala')
     # llamada a la bbdd al nodo de Reservas, con la sala, la fecha de registro y el grupo
    # y guardar los datos en una variable que devolver con render
    registro_data = database.child("Reservas").child(sala).child(registro_date).child(grupo).get().val()
    
    template_name = 'registro/editar_registro.html'
    return render(request, template_name, {'registro_data': registro_data})