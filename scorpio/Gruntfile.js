module.exports = function (grunt) {
  'use strict'; 
  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-bower-requirejs');
  grunt.loadNpmTasks('grunt-bg-shell');
  grunt.loadNpmTasks('grunt-exec');
  grunt.loadNpmTasks('grunt-concurrent');

    grunt.initConfig({
    bgShell: {
      api: {
        cmd: 'nodemon --debug=5858 server.js'
      },
      bootstrapDBs : {
          cmd: 'node databases/bootstrap.js'
      }
    },
    watch: {
      less: {
        files: ['app/less/**/*.less', 'app/less/*.less'],
        tasks: [ 'less:development' ]
      }
    },
    concurrent: {
        serve: {
            tasks: ['watch:less', 'bgShell:api'],
            options: {
                logConcurrentOutput: true
            }
        }
    },
    less: {
      development: {
        src: 'app/less/app.less',
        dest: 'app/_style-development.css'
      }
      // production: {
      //     options: {
      //         yuicompress: true
      //     },
      //     src: 'app/less/app.less',
      //     dest: 'app/_style-production.css'
      // }
    },
    bower: {
      target: {
        rjsConfig: 'app/js/main.js'
      }
    }
  });
  
  grunt.registerTask("serve", [ "less:development", "concurrent:serve"]);
  grunt.registerTask("deploy", [ "less:development", "exec:deploy"]);
  grunt.registerTask("test:unit", [ "exec:unittest"]);
  grunt.registerTask("test", [ "test:unit"]);
  grunt.registerTask("default", [ "serve"]);
  grunt.registerTask("bootstrapDBs", ["bgShell:bootstrapDBs"]);
  
};
