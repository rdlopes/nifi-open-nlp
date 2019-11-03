FROM maven:3.5.2-jdk-8-alpine AS MAVEN_TOOL_CHAIN
COPY . /tmp/
WORKDIR /tmp/
RUN mvn -B clean package -DskipTests

FROM apache/nifi:latest
COPY --from=MAVEN_TOOL_CHAIN /tmp/nifi-nlp-nar/target/nifi-nlp-nar.nar lib/nifi-nlp-nar.nar

#############################################################################################
# !!! container expects a folder containing NLP models in /opt/nifi/nifi-current/models !!! #
#############################################################################################

# USER root
# RUN mkdir $NIFI_HOME/models
# WORKDIR $NIFI_HOME/models

# RUN wget http://opennlp.sourceforge.net/models-1.5/en-ner-date.bin
# RUN wget http://opennlp.sourceforge.net/models-1.5/en-ner-location.bin
# RUN wget http://opennlp.sourceforge.net/models-1.5/en-ner-money.bin
# RUN wget http://opennlp.sourceforge.net/models-1.5/en-ner-organization.bin
# RUN wget http://opennlp.sourceforge.net/models-1.5/en-ner-percentage.bin
# RUN wget http://opennlp.sourceforge.net/models-1.5/en-ner-person.bin
# RUN wget http://opennlp.sourceforge.net/models-1.5/en-ner-time.bin
# RUN wget http://opennlp.sourceforge.net/models-1.5/en-chunker.bin
# RUN wget http://opennlp.sourceforge.net/models-1.5/en-parser-chunking.bin
# RUN wget http://opennlp.sourceforge.net/models-1.5/en-token.bin
# RUN wget http://opennlp.sourceforge.net/models-1.5/en-sent.bin
# RUN wget http://opennlp.sourceforge.net/models-1.5/en-pos-maxent.bin
# RUN wget http://opennlp.sourceforge.net/models-1.5/en-pos-perceptron.bin
# RUN wget https://www-eu.apache.org/dist/opennlp/models/langdetect/1.8.3/langdetect-183.bin
