from django.http import HttpResponse
# from views import open_database
from lasSalas.connection import open_database
import json


def sign_up(request):
    # recoger variables
    form = request.POST
    username = form.get('username')
    password = form.get('pass')

    # llamar a bbdd e inscribir las variables como clave-valor
    database = open_database()
    database.child("Users").child(username).set(password)

    # comprueba si existe el registro y devuelve un formato de respuesta
    user_exist = database.child("Users").child(username).get()
    response = {'status_code': 200, 'username':username} if user_exist.val()\
              else {'status_code': 500, 'username':username}
    
    return HttpResponse(json.dumps(response), content_type="application/json")