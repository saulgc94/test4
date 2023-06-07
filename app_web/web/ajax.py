from django.http import HttpResponse, HttpResponseNotFound
import json
import datetime
from lasSalas.connection import open_database


def listar_musicos(request):
    musicos_list = []
    # llamar a la bbdd y recoger el grupo de session
    database = open_database()
    grupo = request.session['username']
    # recoger todos los datos del grupo hijo del nodo Grupos
    grupo_data = database.child("Grupos").child(grupo).get().val()

    # recorrer los datos y añadir la id autogenerada en el diccionario de valores
    if grupo_data:
        for key, value in dict(grupo_data).items():
            value['id'] = key
            musicos_list.append(value)
    # devolver una respuesta HTTP con un contenido JSON
    return HttpResponse(json.dumps(musicos_list), content_type="application/json")

def listar_musicos_admin(request):
    musicos_list = []
    database = open_database()
    # recoger todos los datos del nodo Grupos
    grupos = database.child("Grupos").get().val()
    # recorrer los datos extraidos y añadir el valor de id autogenerado y
    # el grupo al que pertenecen en el dict de valores
    for key, value in dict(grupos).items():
        for clave, miembro in value.items():
            miembro['id'] = clave
            miembro['grupo'] = key
            musicos_list.append(miembro)
    # devolver una respuesta HTTP con un contenido JSON
    return HttpResponse(json.dumps(musicos_list), content_type="application/json")

def crear_musico_action(request): 
    # recoger variables 
    form = request.POST
    grupo = form.get('grupo')
    nombre = form.get('nombre', '')
    apellido = form.get('apellido', '')
    apellido2 = form.get('apellido2', '')
    edad = form.get('edad', '')
    direccion = form.get('direccion', '')
    dni = form.get('dni', '')

    data = {"nombre": nombre, "apellido": apellido, "apellido2": apellido2, "edad": edad, "direccion": direccion, "dni": dni }
    database = open_database()
    grupo = request.session['username']
    # escribir datos en la bbdd con el dict creado
    database.child("Grupos").child(grupo).push(data)
    # crear un response personalizado para retornar al front
    response = {'status_code': HttpResponse.status_code, 'nombre':nombre, 'apellido': apellido}
    # devolver una respuesta HTTP con un contenido JSON
    return HttpResponse(json.dumps(response), content_type="application/json")

def editar_musico_action(request):
    # recoger variables 
    form = request.POST
    grupo = form.get('grupo')
    nombre = form.get('nombre', '')
    apellido = form.get('apellido', '')
    apellido2 = form.get('apellido2', '')
    edad = form.get('edad', '')
    direccion = form.get('direccion', '')
    dni = form.get('dni', '')
    musico_id = form.get('musico_id', None)
    # comprobar que se ha almacenado correctamente el id sobre el que actuar
    # de lo contrario devolver un error
    if not musico_id:
        return HttpResponseNotFound()

    data = {"nombre": nombre, "apellido": apellido, "apellido2": apellido2, "edad": edad, "direccion": direccion, "dni": dni }
    database = open_database()
    grupo = request.session['username'] if request.session['username'] != "admin" else form.get('grupo')
    response_action = database.child("Grupos").child(grupo).child(musico_id).update(data)
    # crear un response personalizado para retornar al front
    response = {'status_code': 200, 'nombre':nombre, 'apellido': apellido} if data == response_action\
              else {'status_code': 400, 'nombre':nombre, 'apellido': apellido}
    # devolver una respuesta HTTP con un contenido JSON
    return HttpResponse(json.dumps(response), content_type="application/json")

def borrar_musico_action(request): 
    form = request.POST
    musicos_list = json.loads(form.get('musicos_list', []))
    database = open_database()
    response = []
    
    for musico_dict in musicos_list:
        music_id = musico_dict.get('id')
        music_nombre = musico_dict.get('nombre')
        music_apellido = musico_dict.get('apellido')
        grupo = request.session['username'] if request.session['username'] != "admin" else musico_dict.get('grupo')
        database.child("Grupos").child(grupo).child(music_id).remove()
        musico = database.child("Grupos").child(grupo).child(music_id).get()

        response.append({'status_code': 200, 'nombre':music_nombre, 'apellido': music_apellido} if not musico.val()\
              else {'status_code': 400, 'nombre':music_nombre, 'apellido': music_apellido})
    # devolver una respuesta HTTP con un contenido JSON
    return HttpResponse(json.dumps(response), content_type="application/json")


def get_horario(request):
    # recoger datos
    form = request.GET
    sala = form.get('sala', None)
    date = form.get('date', None)
    resp = []
    database = open_database()
    # buscar en la bbdd en el nodo reservas en funcion de los datos
    registro = database.child("Reservas").child(sala).child(date).get().val()
    if registro:
        for grp in registro:
            # recoger las horas reservadas de los grupos y almacenarlo en resp
            horas = [registro[grp].get('in'), registro[grp].get('out')]
            reserva  = {grp: horas}
            resp.append(reserva)
    # devolver una respuesta HTTP con un contenido JSON
    return HttpResponse(json.dumps(resp), content_type="application/json")

def reservar_action(request):
    # recoger variables
    form = request.POST
    start_time = form.get('start_time', None)
    end_time = form.get('end_time', None)
    sala = form.get('sala', None)
    date = form.get('date', None)
    grupo = request.session['username']

    # crear variables de fecha y hora en el formato deseado
    my_date = str(datetime.datetime.today().date()).replace('-', '/')
    my_time = str(datetime.datetime.now().strftime("%X"))

    # comprobación para que no se registre en fechas u horas pasadas
    if my_date > date or ( my_date == date and my_time > start_time):
        response = {'status_code': 400, 'start_time': start_time, 'end_time': end_time, 'error': 'No es posible registrar en fechas o horas pasadas'}
        return HttpResponse(json.dumps(response), content_type="application/json")
    #  comprobar que la hora de entrada es menor que la de salida
    if start_time >= end_time:
        response = {'status_code': 400, 'start_time': start_time, 'end_time': end_time, 'error': 'Error en las horas de reserva'}
        return HttpResponse(json.dumps(response), content_type="application/json")
    
    database = open_database()
    # acción de reservar
    database.child("Reservas").child(sala).child(date).child(grupo).set({'in': start_time, 'out': end_time})
    # comprobar el estado de la acción para crear un response personalizado para retornar al front
    exist_registro = database.child("Reservas").child(sala).child(date).child(grupo).get()
    response = {'status_code': 200, 'start_time':start_time, 'end_time': end_time, 'date': date} if exist_registro.val()\
        else {'status_code': 400, 'error':'Ha habido un error realizando la reserva.'}
    # devolver una respuesta HTTP con un contenido JSON
    return HttpResponse(json.dumps(response), content_type="application/json")

def get_tabla_registro(request):
    # recoger datos
    form = request.GET
    sala = form.get('sala', None)
    year = form.get('year', None)
    month = form.get('month', None)

    registro_list = []
    database = open_database()
    grupo = request.session['username']
    # buscar en el nodo reservas de bbdd por lasvariables recogidas
    registro_data = database.child("Reservas").child(sala).child(year).child(month).get()
    # si no encuentra datos es que no hay registros y devuelve vacio
    if not registro_data.val():
        return HttpResponse(json.dumps([]), content_type="application/json")
    # recorrer los registros de reservas, darle el formato deseado y almacenarlo en registro_list
    for reg_day in registro_data.each():
        if grupo in reg_day.val():
            registration = reg_day.val()
            data = year+'/'+month+'/'+reg_day.key()
            registro_list.append({'sala': sala, 'nombre': grupo, 'date': data, 'in': registration[grupo]['in'], 'out': registration[grupo]['out'] })
    # devolver una respuesta HTTP con un contenido JSON
    return HttpResponse(json.dumps(registro_list), content_type="application/json") 

def get_tabla_registro_admin(request):
    # recoger datos
    form = request.GET
    sala = form.get('sala', None)
    year = form.get('year', None)
    month = form.get('month', None)

    registro_list = []
    database = open_database()
    # buscar en el nodo reservas de bbdd por lasvariables recogidas
    registro_data = database.child("Reservas").child(sala).child(year).child(month).get()
    # si no encuentra datos es que no hay registros y devuelve vacio
    if not registro_data.val():
        return HttpResponse(json.dumps([]), content_type="application/json")
    # recorrer los registros de reservas, darle el formato deseado y almacenarlo en registro_list
    for reg_day in registro_data.each():
        registration = reg_day.val()
        grupos = registration.keys()
        data = year+'/'+month+'/'+reg_day.key()
        for grupo in grupos:
            registro_list.append({'sala': sala, 'grupo': grupo, 'date': data, 'in': registration[grupo]['in'], 'out': registration[grupo]['out'] })
    # devolver una respuesta HTTP con un contenido JSON
    return HttpResponse(json.dumps(registro_list), content_type="application/json") 

def borrar_registro_action(request):
    # recoger datos
    form = request.POST
    sala = form.get('sala', None)
    registros_list = json.loads(form.get('registros_list', []))
    database = open_database()
    response = []
    # crear variables de fecha y hora en el formato deseado
    my_date = str(datetime.datetime.today().date()).replace('-', '/')
    my_time = str(datetime.datetime.now().strftime("%X"))

    # recorrer los registros recogidos para la acción
    for registro in registros_list:
        # determinar si la variable grupo está recogida por session o request.post en función de si el usuario es admin
        grupo = request.session['username'] if request.session['username'] != "admin" else registro.get('grupo')
        registro_date = registro.get('date')
        # comprobación para no eliminar registros de fechas u horas pasadas
        if my_date > registro_date or ( my_date == registro_date and my_time > registro.get('in')):
            response.append({'status_code': 400, 'grupo':grupo, 'registro_data': registro, 'error': 'No es posible borrar registros pasados'})
            continue
        # acción de borrado
        database.child("Reservas").child(sala).child(registro_date).child(grupo).remove()
        # comprobar si se ha realizado la accion para crear un response personalizado para retornar al front
        registro_exist = database.child("Reservas").child(sala).child(registro_date).child(grupo).get()
        response.append({'status_code': 200, 'grupo':grupo, 'registro_date': registro_date} if not registro_exist.val()\
              else {'status_code': 400, 'grupo':grupo, 'registro_date': registro_date, 'error': 'Error borrando registro.'})
    # devolver una respuesta HTTP con un contenido JSON
    return HttpResponse(json.dumps(response), content_type="application/json")


def editar_registro_action(request):
    # recoger variables
    form = request.POST
    registro_date = form.get('registro_date')
    old_registro_in = form.get('old_registro_in')
    registro_in = form.get('start-time')
    registro_out = form.get('end-time')
    sala = form.get('sala')
    grupo = request.session['username'] if request.session['username'] != "admin" else form.get('grupo')

    # crear variables de fecha y hora en el formato deseado
    my_date = str(datetime.datetime.today().date()).replace('-', '/')
    my_time = str(datetime.datetime.now().strftime("%X"))

    response = {}
    horas_reservadas = []
    database = open_database()

    # extraer los horas disponibles del día del registro
    registro_dia = database.child("Reservas").child(sala).child(registro_date).get().val()
    if registro_dia:
        for grp in registro_dia:
            if grp != grupo:
                horas = [registro_dia[grp].get('in'), registro_dia[grp].get('out')]
                horas_reservadas+= horas

    # comprobar que no se edita sobre horas no disponibles
    if registro_in in horas_reservadas or registro_out in horas_reservadas:
        response = {'status_code': 400, 'grupo':grupo, 'registro_data': registro_date, 'error': 'Selecciona un horario disponible'}
        return HttpResponse(json.dumps(response), content_type="application/json")
    # comprobar que no se puede editar registros pasados de fecha u hora
    if my_date > registro_date or ( my_date == registro_date and my_time > registro_in) or ( my_date == registro_date and my_time > old_registro_in):
        response = {'status_code': 400, 'grupo':grupo, 'registro_data': registro_date, 'error': 'No es posible editar registros pasados'}
        return HttpResponse(json.dumps(response), content_type="application/json")
    #  comprobar que la hora de entrada es menor que la de salida
    if registro_in >= registro_out:
        response = {'status_code': 400, 'error': 'Error en las horas de reserva'}
        return HttpResponse(json.dumps(response), content_type="application/json")

    data = {"in": registro_in, "out": registro_out}
    # acción de editar
    response_action = database.child("Reservas").child(sala).child(registro_date).child(grupo).update(data)
    # comprobar si se ha realizado la accion para crear un response personalizado para retornar al front
    response = {'status_code': 200, 'registro_date':registro_date} if data == response_action\
              else {'status_code': 400, 'registro_date':registro_date, 'error': 'Ha habido un error editando la reserva'}
    # devolver una respuesta HTTP con un contenido JSON
    return HttpResponse(json.dumps(response), content_type="application/json")