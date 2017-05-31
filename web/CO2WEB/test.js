/**
 * Created by Shaocong on 4/4/2017.
 */
/*******IMPORT**********/
var expect = require("chai").expect;
var util = require("util");
var rdfParser = require(__dirname + "/rdfParser.js");
var request = require("request");
var path = require("path");
var connections = require("./fileConnection.js");
var opts = {
    fileUrl: __dirname + "/testFile/JurongIsland.owl",
    uri: 'http://www.theworldavatar.com/JurongIsland.owl'
};

var config = require("./config");
var baseURL = "http://localhost:" + config.port;


var app = require("./app.js");


describe('RDFParser: ', function () {

    it('could find the value of property of an indivdiual in a specific file', function () {


        let node = 'http://www.theworldavatar.com/JurongIsland.owl#V_CO2_Jurong';
        let property = "http://www.theworldavatar.com/OntoCAPE/OntoCAPE/upper_level/system.owl#numericalValue";
        let result = rdfParser(opts).search(node, property);


        expect(result.value).to.equal('1500.0');

    })


});





describe("extract connections", function () {
    it('find all connections of each file', function (done) {

        connections(0, function(err, results){

            if(err){
                console.log(err);
                done(err);
            }
            console.log(JSON.stringify(results));
            expect(results).to.be.a('array');
            expect(results).to.have.length.above(0);
            for(let conn of results){
                expect(conn).to.have.property('source');
                expect(conn).to.have.property('target');
                expect(conn).to.have.property('level');

            }

            done();

        })
    }).timeout(1000);
});


describe('get /visualize', function () {

    it('returns status 200',function (done) {
        let url = baseURL + "/visualize";
        console.log("request "+ url);

        request.get({url : url}, function(error, response, body) {
            if(error){
                console.log(error);
                done(error);
            }
            expect(response.statusCode).to.equal(200);


            done();
        })

    })

});

describe('get /visualize/includeImport', function(){

    it('returns json', function(){
        let url = baseURL + "/visualize/includeImport";
        console.log("request "+ url);

        request.get({url : url}, function(error, response, body) {
            if(error){
                console.log(error);
                done(error);

            }
            expect(response.statusCode).to.equal(200);
            let results = JSON.parse(body);
            expect(results).to.be.a('array');
            expect(results).to.have.length.above(0);
            for(let conn of results){
                expect(conn).to.have.property('source');
                expect(conn).to.have.property('target');

            }

            done();
        })

    })
});

describe('get /JurongIsland.owl/showCO2', function(){
    it('returns status 200',function (done) {
        let url = baseURL + "/JurongIsland.owl/showCO2";
        console.log("request "+ url);

        request.get({url : url}, function(error, response, body) {
            if(error){
                console.log(error);
                done(error);
            }
            expect(response.statusCode).to.equal(200);


            done();
        })

    })

});
