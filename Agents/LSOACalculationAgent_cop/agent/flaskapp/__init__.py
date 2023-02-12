# Create Flask application and import (i.e. register) HTTP routes

from flask import Flask

def create_app(test_config=None):
    """
    Create and configure an instance of the Flask application.
    """

    app = Flask(__name__, instance_relative_config=True)
    app.config['JSON_SORT_KEYS'] = False
    if test_config is not None:
        # Load the test config if passed in
        app.config.update(test_config)

    with app.app_context():
        # Import parts of application
        import agent.flaskapp.home.routes as home
        import agent.flaskapp.calculationtask.routes as calculationtask
        # Register Blueprints
        app.register_blueprint(home.home_bp)
        app.register_blueprint(calculationtask.calculationtask_bp)

    return app