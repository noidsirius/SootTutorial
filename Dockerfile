FROM openjdk:8
RUN mkdir /android-cmdline-tools
RUN mkdir /android-sdk
RUN curl https://dl.google.com/android/repository/commandlinetools-linux-6609375_latest.zip > /android-cmdline-tools/cmdline-tools.zip && unzip -qq -n /android-cmdline-tools/cmdline-tools.zip -d /android-cmdline-tools
RUN echo y | /android-cmdline-tools/tools/bin/sdkmanager --sdk_root=/android-sdk 'platform-tools' > /dev/null && echo y | /android-cmdline-tools/tools/bin/sdkmanager --sdk_root=/android-sdk 'build-tools;29.0.3' > /dev/null && echo y | /android-cmdline-tools/tools/bin/sdkmanager --sdk_root=/android-sdk 'platforms;android-29' > /dev/null
ENV ANDROID_HOME /android-sdk/
WORKDIR /app
COPY . /app
RUN /app/gradlew build

