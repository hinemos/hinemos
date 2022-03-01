// オリジナルプログラム: https://www.npmjs.com/package/date-with-offset
// オリジナルバージョン: v1.1.0
// オリジナル製作者: Copyright 2013 James A. Rosen
// ライセンス: Apache License, Version 2.0

// 改変者: NTT DATA INTELLILINK Corporation
// 改変内容:
//   - ビルドインのDateクラスを、DateWithOffsetで置き換えるようにした。
//     オフセット値はコンストラクタの最後の引数であるが、デフォルト値はgetGraphMessages()から取得する。
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
  // タイムゾーンのオフセット最大値（分）
  var OFFSET_MAX = 14 * 60;
  // 年の最小値
  var YEAR_MIN = 1900;
  // 年の最大値（この値は含まない）
  var YEAR_MAX = 3000;

  var dateOffset;
  
  function isNumber(x) { return typeof(x) === 'number'; }

  function isFunction(x) { return typeof(x) === 'function'; }

  function isOffset(x) {
    if (x == null) { return false; }
    if (isFunction(x.valueOf)) {
      x = x.valueOf();
    }
    if (!isNumber(x)) { return false; }
    // タイムゾーンオフセット値か、時刻値かを判定する
    return x <= OFFSET_MAX;
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
      if (args.length === 2) {
        date = new OrgDate(args[0], args[1]);
      } else {
        args[3] = args[3] || null;
        args[4] = args[4] || null;
        args[5] = args[5] || null;
        args[6] = args[6] || null;

        date = new OrgDate(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
      }
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

  /**
   * コンストラクタ
   * 
   * Dateのタイムゾーン拡張のため、引数最後にタイムゾーンオフセット(分)を任意で設定可能。
   * それ以外の引数はDateとほぼ同じ。
   * cf) https://developer.mozilla.org/ja/docs/Web/JavaScript/Reference/Global_Objects/Date/Date
   * 以下の通り、引数の数、型、値で引数の種類を判定する。
   *
   * 引数パターン
   * 引数0個）引数なし。現在日時設定を設定。タイムゾーンオフセットは、"mess-timezoneoffset"が設定されていればその値、されていなければローカルの値とする。
   * 引数1個）タイムゾーンオフセット値（分）：数値かつ、<= 840(=14*60) の場合
   *          時刻値（ミリ秒）：数値かつ、 > 840 の場合
   *          タイムスタンプ文字列：文字列の場合
   *          Dateオブジェクト：オブジェクトの場合
   * 引数2個）独立した日付と時刻の成分の値2個（年、月）：第1引数が、数値かつ、>= 1900 かつ < 3000 の場合
   *          時刻値、タイムゾーンのオフセット値（分）：第1引数が、数値かつ、< 1900 または >= 3000 の場合
   *          タイムスタンプ文字列、タイムゾーンのオフセット値（分）
   *          Dateオブジェクト、タイムゾーンのオフセット値（分）
   * 引数3～7個）独立した日付と時刻の成分の値3～7個（年、月、日、時、分、秒、ミリ秒）
   * 引数8個）独立した日付と時刻の成分の値7個（年、月、日、時、分、秒、ミリ秒）、タイムゾーンのオフセット値（分）
   */
  function DateWithOffset() {
    var args = slice.call(arguments, 0);
    var aDate = new OrgDate();
    var offset = - aDate.getTimezoneOffset();
    if (dateOffset == null) {
      if (typeof getGraphMessages == "function") {		// isFunction()を使うとエラー
        dateOffset = getGraphMessages("mess-timezoneoffset") - 0;
      }
    }

    switch (arguments.length) {
      case 0:
          if (dateOffset != null) {
            offset = dateOffset;
          }
        break;
      case 1:
        if (isOffset(args[0])) {
          // タイムゾーンのオフセット値
          offset = args.pop();
        } else {
          // 時刻値の場合
          // タイムスタンプ文字列の場合
          // Dateオブジェクトの場合
          if (dateOffset != null) {
            offset = dateOffset;
          }
        }
        break;
      case 2:
        // 独立した日付と時刻の成分の値2個（年、月）の場合
        if (isNumber(args[0]) && args[0] >= YEAR_MIN && args[0] < YEAR_MAX) {
          if (dateOffset != null) {
            offset = dateOffset;
          }
          break;
        }
        // 時刻値、タイムゾーンのオフセット値の場合
        // タイムスタンプ文字列、タイムゾーンのオフセット値の場合
        // Dateオブジェクト、タイムゾーンのオフセット値の場合
        // 下へ続行
      case 8:
        // 独立した日付と時刻の成分の値7個、タイムゾーンのオフセット値の場合
        offset = args.pop();
        if (!isOffset(offset)) {
          // offsetが異常
          throw 'invalid offset.';
        }
        break;
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
        // 独立した日付と時刻の成分の値7個の場合
        if (dateOffset != null) {
          offset = dateOffset;
        }
        break;
      default:
        // 引数が異常
        throw 'invalid arguments.';
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