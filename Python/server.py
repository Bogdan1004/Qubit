
                                            #~~~~~~~#
                                            # Qubit #
                                            #~~~~~~~#
from flask import Flask
from flask import jsonify
app = Flask(__name__)


@app.route('/')
def index():
    return ' Qubit a ajuns la etapa nationala la Galaciuc ! '

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#
# Afiseaza coordonatele citite din fisierul txt intr-un format JSON #
#pe serverul web local                                              #
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#
@app.route('/coord')
def summary():
    file = open("testfile.txt", "r+")
    string = file.read()
    print(string)
    string.split(",")
    coordX, coordY = string.split(',')
    file.close()
    return jsonify(coordX, coordY)

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')


                              #~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#
                              # Signed by Qubit (Preda Bogdan)#
                              #~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#