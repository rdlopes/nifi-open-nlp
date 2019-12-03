# nifi-open-nlp

A set of [NiFi](https://nifi.apache.org) processors implementing [Apache OpenNLP](https://opennlp.apache.org/) engine tools.

[![CI](https://github.com/rdlopes/nifi-open-nlp/workflows/Continuous%20Integration/badge.svg)](https://github.com/rdlopes/nifi-open-nlp/actions)

[![CD](https://github.com/rdlopes/nifi-open-nlp/workflows/Continuous%20Deployment/badge.svg)](https://github.com/rdlopes/nifi-open-nlp/actions)

[![Quality](https://sonarcloud.io/api/project_badges/measure?project=org.rdlopes%3Anifi-open-nlp&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.rdlopes%3Anifi-open-nlp)

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

#### NLP models folder

A new NiFi folder exists under `$NIFI_HOME/models` that contains the pre-trained
models for English language:

* `en-chunker.bin`
* `en-doccat-tweets.bin`
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
* `langdetect-183.bin`

#### NLP training

A new NiFi folder exists under `$NIFI_HOME/training` that contains `tweets.txt`, an example of training data
for sentiment analysis on tweets (see [Document Categorizer](https://opennlp.apache.org/docs/1.9.1/manual/opennlp.html#tools.doccat))
taken from [this discussion](https://stackoverflow.com/questions/44781094/sentiment-analysis-with-opennlp) on StackOverflow.

#### NLP model store

Another new folder under `$NIFI_HOME/model-store` is present and will hold the trained models for the processors.

The rationale is that processors can be trained using both model files, training files and training data so input types differ, 
but at the end of the day, it all ends in a model file that can be stored and reused by the processors. Lifecycle of processors
training/evaluation will be explained further.

## Importing from [Jitpack](https://jitpack.io/)

Add Jitpack repository in your maven project:

	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
	
and the maven dependency on the github project:

    <dependency>
	    <groupId>com.github.rdlopes</groupId>
	    <artifactId>nifi-open-nlp</artifactId>
	    <version>${nifi-open-nlp.version}</version>
	    <type>nar</type>
	</dependency>
	
## Importing from [GitHub Package Registry](https://github.com/features/package-registry)

The feature is temporarily disabled, I'm waiting for GitHub feedback on few issues.

## Apache NLP tools

Following tools listed in the 
[OpenNLP developer documentation](https://opennlp.apache.org/docs/1.9.1/manual/opennlp.html) 
are implemented:

* [Language Detector](https://opennlp.apache.org/docs/1.9.1/manual/opennlp.html#tools.langdetect)
* [Sentence Detector](https://opennlp.apache.org/docs/1.9.1/manual/opennlp.html#tools.sentdetect)
* [Tokenizer](https://opennlp.apache.org/docs/1.9.1/manual/opennlp.html#tools.tokenizer)
* [Name Finder](https://opennlp.apache.org/docs/1.9.1/manual/opennlp.html#tools.namefind)
* [Document Categorizer](https://opennlp.apache.org/docs/1.9.1/manual/opennlp.html#tools.doccat)
* [Part-of-Speech Tagger](https://opennlp.apache.org/docs/1.9.1/manual/opennlp.html#tools.postagger)
* [Lemmatizer](https://opennlp.apache.org/docs/1.9.1/manual/opennlp.html#tools.lemmatizer)
* [Chunker](https://opennlp.apache.org/docs/1.9.1/manual/opennlp.html#tools.chunker)
* [Parser](https://opennlp.apache.org/docs/1.9.1/manual/opennlp.html#tools.parser)

For further documentation, please refer to processors usage page.
