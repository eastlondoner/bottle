Scorpio
========


Setup
-----

You'll need Node.JS installed (we use it for the build scripts, and will use it for the server parts as well). You can get it here: http://nodejs.org/ or install with Homebrew (`brew install nodejs`)

To install the build script requirements run from the project directory:

    npm install
    npm install -g grunt-cli 
    npm install -g nodemon
    npm install -g bower
    bower install

To start the build watcher and demo server run:

    grunt

Then open in your browser: http://127.0.0.1:8000
