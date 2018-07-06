// オリジナルプログラム: https://www.npmjs.com/package/date-with-offset
// オリジナルバージョン: v1.1.0
// オリジナル製作者: Copyright 2013 James A. Rosen
// ライセンス: Apache License, Version 2.0

// 改変者: NTT DATA INTELLILINK Corporation
// 改変内容:
//   - ビルドインのDateクラスを、DateWithOffsetで置き換えるようにした。
//     それに伴い、オフセット値をコンストラクタの最後の引数ではなく、getGraphMessages()から取得するようにした。
//     Dateが提供していたstaticメソッドをカバーするため、とりあえずHinemosが使用している'now'のみ、実装を追加した。
//   - 複数の引数を取るsetterに対応した。
// 補足:
//   DateWithOffsetを改変せずに、サブクラスを導出してそちらでHinemos固有の実装を行いたかったが、
//   DateWithOffsetが内部でDateを使用しているため、Dateの置き換えをするには改変が必要だった。

(function(window, undefined) {

  "use strict";

  var slice = Array.prototype.slice,
      MILLISECONDS_PER_MINUTE = 60 * 1000,
      OFFSET_SUFFIX = /(((GMT)?[\+\-]\d\d:?\d\d)|Z)(\s*\(.+\))?$/;

  var dateOffset;
  
  function isNumber(x) { return typeof(x) === 'number'; }

  function isFunction(x) { return typeof(x) === 'function'; }

  function isOffset(x) {
    if (x == null) { return false; }
    if (isFunction(x.valueOf)) {
      x = x.valueOf();
    }
    return isNumber(x);
  }

  function applyOffset(date, offset) {
    date.setTime( date.getTime() + MILLISECONDS_PER_MINUTE * offset );
    return date;
  }

  function buildDate(args, offset) {
    if (args.length === 0) { return new OrgDate(); }

    if (args.length === 1 && args[0] instanceof OrgDate) { return args[0]; }
    if (args.length === 1 && isNumber(args[0]))       { return new OrgDate(args[0]); }

    if (args.length > 1) {
      args[3] = args[3] || null;
      args[4] = args[4] || null;
      args[5] = args[5] || null;
      args[6] = args[6] || null;

      date = new OrgDate(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
      return applyOffset( date, -date.getTimezoneOffset() - offset );
    }

    var string            = args[0].toString(),
        date              = new OrgDate(string),
        isYYYYmmdd        = /\d\d\d\d-\d\d-\d\d/.test(string),
        isOffsetSpecified = OFFSET_SUFFIX.test(string),
        isLocal           = !isYYYYmmdd && !isOffsetSpecified;

    if (isLocal) {
      date = applyOffset(date, -date.getTimezoneOffset() - offset);
    }

    return date;
  }

  function formattedOffset(offsetInMinutes) {
    var sign    = offsetInMinutes >= 0 ? '+' : '-';
    offsetInMinutes = Math.abs(offsetInMinutes);
    var hours   = Math.floor(offsetInMinutes / 60),
        minutes = offsetInMinutes - 60 * hours;
    if (hours < 10)   { hours = '0' + hours; }
    if (minutes < 10) { minutes = '0' + minutes; }
    return 'GMT' + sign + hours + minutes;
  }

  function DateWithOffset() {
        var args = slice.call(arguments, 0);
    var offset = dateOffset;

    if (offset === undefined) {
      if (typeof getGraphMessages == "function") {
        offset = dateOffset = getGraphMessages("mess-timezoneoffset") - 0;
      } else {
        offset = 0;
      }
    }

    this.setTime(buildDate(args, offset));
    this.offset = function() { return offset; };
  }

  DateWithOffset.prototype = {

    // A Date whose UTC time is the local time of this object's real time.
    // That is, it is incorrect by `offset` minutes. Used for `getDate` et al.
    localDate: function() {
      return applyOffset(this.date(), this.offset());
    },

    withOffset: function(offset) {
      return new DateWithOffset(this.getTime(), offset);
    },

    getTime:            function() { return this.date().getTime(); },
    getTimezoneOffset:  function() { return -this.offset(); },
    toISOString:        function() { return this.date().toISOString(); },
    valueOf:            function() { return this.getTime(); },
    toJSON:             function() { return this.toISOString(); },

    toString: function() {
      var localDate = this.localDate(),
          plusBrowserOffset = applyOffset(localDate, localDate.getTimezoneOffset()),
          asString = plusBrowserOffset.toString();
      return asString.replace(OFFSET_SUFFIX, formattedOffset(this.offset()));
    },

    getYear: function() {
      return this.localDate().getUTCFullYear() - 1900;
    },

    setYear: function(year) {
      return this.setFullYear(1900 + year);
    },

    setTime: function(date) {
      this.date = function() { return new OrgDate(date); };
      return this;
    }

  };

  function addGetters(property) {
    DateWithOffset.prototype['get' + property] = function() {
      return this.localDate()['getUTC' + property]();
    };

    DateWithOffset.prototype['getUTC' + property] = function() {
      return this.date()['getUTC' + property]();
    };
  }

  function addSetters(property) {
    DateWithOffset.prototype['set' + property] = function() {
      var localDate = this.localDate();
      localDate['setUTC' + property].apply(localDate, arguments);
      return this.setTime( applyOffset(localDate, -this.offset()) );
    };

    DateWithOffset.prototype['setUTC' + property] = function() {
      var date = this.date();
      date['setUTC' + property].apply(date, arguments);
      return this.setTime(date);
    };
  }

  addGetters('Date');           addSetters('Date');
  addGetters('Day');            // can't set day of week
  addGetters('FullYear');       addSetters('FullYear');
  addGetters('Hours');          addSetters('Hours');
  addGetters('Milliseconds');   addSetters('Milliseconds');
  addGetters('Minutes');        addSetters('Minutes');
  addGetters('Month');          addSetters('Month');
  addGetters('Seconds');        addSetters('Seconds');

  DateWithOffset.now = function() {
    return new DateWithOffset();
  }

  if (typeof(module) !== 'undefined') {
    module.exports = DateWithOffset;
  }

  if (typeof(window) !== 'undefined') {
    window.DateWithOffset = DateWithOffset;
  }

  var OrgDate = window.Date;
  window.Date = DateWithOffset;

}(this));