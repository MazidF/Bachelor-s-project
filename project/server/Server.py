import mimetypes
import os
from datetime import datetime

from flask import Flask, request, jsonify

# from models.model1.DetectorModel1 import DetectorModel1
# Disabled because of torch conflict with CuDNN (model 3)

from models.model2.DetectorModel2 import DetectorModel2
from models.model3.DetectorModel3 import DetectorModel3


class Server:
    def __init__(self, upload_folder='uploads', host='127.0.0.1', port=5000):
        self.app = Flask(__name__)
        self.upload_folder = upload_folder
        self.host = host
        self.port = port

        # self.model1 = DetectorModel1()
        # Disabled because of torch conflict with CuDNN (model 3)

        self.model2 = DetectorModel2()
        self.model3 = DetectorModel3()

        os.makedirs(self.upload_folder, exist_ok=True)
        self.app.config['UPLOAD_FOLDER'] = self.upload_folder

        # Register routes
        self.app.add_url_rule('/upload', 'upload_file', self.upload_file, methods=['POST'])

    def upload_file(self):
        audio_file = self.extract_file()
        if audio_file is None:
            return jsonify({
                'model': request.values.get('model'),
                'error': 'No file part in the request'
            }), 200  # For simplicity of error handling.

        model, model_name = self.choose_model()
        if not model:
            return jsonify({
                'model': request.values.get('model'),
                'error': 'Please select another model to process.'
            }), 200

        age = None
        if model_name == 'model1':
            age = self.extract_age()
            if age is None:
                return jsonify({
                    'model': request.values.get('model'),
                    'error': 'No age argument in the request'
                }), 200

        try:
            if age is not None:
                prediction = model.predict(audio_file, age)
            else:
                prediction = model.predict(audio_file)

            return jsonify({
                'message': 'File successfully processed',
                'status': float(prediction[0]),
                'model': model_name
            }), 200
        except Exception as e:
            return jsonify({
                'message': 'File could not be processed',
                'error': str(e),
            }), 500
        finally:
            os.remove(audio_file)

    def extract_file(self):
        audio_file = request.files['audio']
        if audio_file is None:
            return None

        self.model2 = self.model3
        content_type = request.content_type
        extension = mimetypes.guess_extension(content_type)
        if extension is None:
            extension = '.wav'
        print(f'{content_type=}, {extension=}')

        file_path = f'{self.upload_folder}/{datetime.now()}{extension}'
        with open(file_path, 'wb') as file:
            file.write(audio_file.read())
            file.flush()

        return file_path

    def extract_age(self):
        return request.values.get('age')

    def choose_model(self):
        model_name = request.values.get('model').lower()
        match model_name:
            case 'model1':
                return None, model_name  # Disabled because of torch conflict with CuDNN (model 3)
            case 'model2':
                return self.model2, model_name
            case _:
                return self.model3, 'model3'

    def run(self):
        self.app.run(host=self.host, port=self.port, debug=True)
