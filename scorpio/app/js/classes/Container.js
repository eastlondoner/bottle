define("Container",[], function () {
    'use strict';
    /**
     * Container class
     */
    function Container(obj) {
        _.extend(this, obj);
        if (!this.validate()) {
            throw "Invalid Container Defn";
        }
    }

    Container.prototype.validate = function () {
        var props = ['name'];
        var checkTypes = _.every(props, _.isString);
        var checkNotEmpty = !_.some(props, _.isEmpty);
        return checkTypes && checkNotEmpty;
    };

    return Container;
});
