import pyrebase

# Función para la conexión a la bbdd
def open_database():
    firebaseConfig = {
        "apiKey": "AIzaSyAfV1ooM1ZhqA2cjUVOuKXiOMRl1taP9Zc",
        "authDomain": "prueba-6f6bb.firebaseapp.com",
        "databaseURL": "https://prueba-6f6bb-default-rtdb.europe-west1.firebasedatabase.app",
        "projectId": "prueba-6f6bb",
        "storageBucket": "prueba-6f6bb.appspot.com",
        "messagingSenderId": "123477974229",
        "appId": "1:123477974229:web:0e4391e64abbbdab7c165c",
    }
    firebase = pyrebase.initialize_app(firebaseConfig)
    database = firebase.database()
    return database