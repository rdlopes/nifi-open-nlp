# nifi-open-nlp

A set of [NiFi](https://nifi.apache.org) processors implementing [Apache OpenNLP](https://opennlp.apache.org/) engine tools.

## Project structure

Project has been generated using Maven archetype 
`org.apache.nifi:nifi-processor-bundle-archetype:1.8.0`

It is a Java 8 project built by Maven 3.3+ and following Maven layout conventions.

One can find a docker-compose setup to run NiFi locally with a predefined workflow, 
present as examples of use.

## Building & running

You can build project then reuse the nar file produced in your NiFi or boot a Docker
container ready to use. 

### From sources

Maven commands are available to build the project, using

    mvn clean package

This will run the tests  locally and prepare a nar file that you can drop into 
your current nifi install, should you have one.

### Inside Docker container

Simply run the docker-compose file using

    docker-compose up

Build is done inside the container, as a separate maven layer, so expect to wait 
a few seconds for Maven to download the internet.

Then the nar file is copied into NiFi `lib/` folder and NiFi is started as a container, 
available on the port 8080.

The configuration directory for NiFi (`$NIFI_HOME/conf` or `/opt/nifi/nifi-current/conf`)
has been mapped to the local folder `./nifi-local-data/conf`.

A new NiFi folder exists under `$NIFI_HOME/models` that contains the pre-trained
models for English language:

* `en-chunker.bin`
* `en-ner-date.bin`
* `en-ner-location.bin`
* `en-ner-money.bin`
* `en-ner-organization.bin`
* `en-ner-percentage.bin`
* `en-ner-person.bin`
* `en-ner-time.bin`
* `en-parser-chunking.bin`
* `en-pos-maxent.bin`
* `en-pos-perceptron.bin`
* `en-sent.bin`
* `en-token.bin`
