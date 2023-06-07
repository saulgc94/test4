from django.shortcuts import redirect, render
from django.urls import reverse
from lasSalas.connection import open_database


# Create your views here.
def login(request):
    # limpiar la session cuando entra al login
    request.session.flush()
    template_name = 'login/login.html'
    return render(request, template_name)

def auth_log(request):
    # recoger datos de login
    form = request.POST
    username = form.get('username')
    password = form.get('pass')
    # sacar de bbdd los usuario
    database = open_database()
    all_users = dict(database.child("Users").get().val())
    # comprobar los datos de login dando 3 casos posibles
    # no existe el usuario
    if username not in all_users:
        # determinar el template
        template_name = 'login/login.html'
        return render(request, template_name,{'error': 'No existe el usuario'})
    # login correcto
    if all_users[username] == password:
        request.session['username'] = username
        request.session.modified = True
        return redirect(reverse('web'))
    # contraseña incorrecta
    else:
        # determinar el template
        template_name = 'login/login.html'
        return render(request, template_name, {'error': 'Contraseña incorrecta'})

def sign_up(request):
    template_name = 'sign_up/sign_up.html'
    return render(request, template_name)

def sign_up_action(request):
    # recoger datos de la inscripcion
    form = request.POST
    username = form.get('username')
    password = form.get('password')
    # determinar el template
    template_name = 'sign_up/sign_up.html'

    if username == "admin":
        return render(request, template_name, {'response': 'Error inscribiendo usuario, perfil de administrador reservado', 'created': False})

    database = open_database()
    database.child("Users").child(username).set(password)

    user_exist = database.child("Users").child(username).get()
    if user_exist.val():
        return render(request, template_name, {'response': 'Usuario creado', 'created': True})
    else:
        return render(request, template_name, {'response': 'Error inscribiendo usuario', 'created': False})