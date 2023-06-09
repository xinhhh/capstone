from flask import Blueprint, request, jsonify, make_response

import ontomatch
import flaskapp.parameterVerifier.verifier as verifier

geonames_bp = Blueprint(
    'geonames_bp', __name__
)

# Define a route for API requests
@geonames_bp.route('/api/geonames/query', methods=['POST'])
def api():
    # TODO: Check arguments validity(query parameters)
    print(request.args)


    try:
        # Check parameters
        if 'country' not in request.args or 'name' not in request.args:
            raise Exception
        country = request.args['country']
        name = request.args['name']
        verifier.verifyChoice(country, ["germany","unitedkingdom"])

        # Run the agent
        agent = ontomatch.knowledge.geoNames.Agent(country)
        lat, long = agent.query(name)
        response = {}
        response["lat"] = lat
        response["lng"] = long
        return jsonify({"result": response})

    except Exception as ex:
        print(ex)
        return jsonify({'errormsg': 'Invalid request'}), 500
