package com.websarva.wings.android.databasesample

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    // 選択されたカクテルの主キーIDを表すプロパティ
    private var _cocktailId = -1

    // 選択されたカクテル名を表すプロパティ
    private var _cocktailName = ""

    // データベースヘルパーオブジェクト
    private val _helper = DatabaseHelper(this@MainActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // カクテルリスト用ListViewを取得
        val lvCocktail = findViewById<ListView>(R.id.lvCocktail)
        // lvCocktailにリスナを登録
        lvCocktail.onItemClickListener = ListItemClickListener()
    }

    override fun onDestroy() {
        _helper.close()
        super.onDestroy()
    }

    // 保存ボタンがタップされたときの処理メソッド
    fun onSaveButtonClick() {
        // 感想欄を取得
        val etNote = findViewById<EditText>(R.id.etNote)
        // 入力された感想を取得
        val note = etNote.text.toString()

        // データベースヘルパーオブジェクトからデータベース接続オブジェクトを取得
        val db = _helper.writableDatabase

        // まず、リストで選択されたカクテルのメモデータを削除。その後INSERTを行う
        // 削除用SQL文字列を用意
        val sqlDelete = "DELETE FROM cocktailmemos WHERE _id = ?"
        // SQL文字列を元にプリペアドステートメントを取得
        var stmt = db.compileStatement(sqlDelete)
        // 変数のバイド
        stmt.bindLong(1, _cocktailId.toLong())
        // 削除SQL実行
        stmt.executeUpdateDelete()

        // INSERT用SQL文字列の用意
        val sqlInsert = "INSERT INTO cocktailmemos (_id, name, note) VALUES (?, ?, ?)"
        // SQL文字列を元にプリペアドステートメントを取得
        stmt = db.compileStatement(sqlInsert)
        // 変数のバイド
        stmt.bindLong(1, _cocktailId.toLong())
        stmt.bindString(2, _cocktailName)
        stmt.bindString(3, note)
        // インサートSQLの実行
        stmt.executeInsert()

        // 感想欄の入力値を消去
        etNote.setText("")
        // カクテル名を表示するTextViewを取得
        val tvCocktailName = findViewById<TextView>(R.id.tvCocktailName)
        // カクテル名を「未選択」に変更
        tvCocktailName.text = getString(R.string.tv_name)
        // 保存ボタンを取得
        val btnSave = findViewById<Button>(R.id.btnSave)
        // 保存ボタンをタップできないように変更
        btnSave.isEnabled = false
    }

    // リストがタップされたときの処理
    private inner class ListItemClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            // タップされた行番号をプロパティの主キーIDに代入
            _cocktailId = position
            // タップされた行のデータを取得。これがカクテル名となるので、プロパティに代入
            _cocktailName = parent.getItemAtPosition(position) as String
            // カクテル名を表示するTextViewを取得
            val tvCocktailName = findViewById<TextView>(R.id.tvCocktailName)
            // カクテル名を表示するTextViewに表示カクテル名を取得
            tvCocktailName.text = _cocktailName
            // 保存ボタンを取得
            val btnSave = findViewById<Button>(R.id.btnSave)
            // 保存ボタンをタップできるように変更
            btnSave.isEnabled = true

            // データベースヘルパーオブジェクトからデータベース接続オブジェクトを取得
            val db = _helper.writableDatabase
            // 主キーによる検索SQL文字列の用意
            val sql = "SELECT * FROM cocktailmemos WHERE _id = ?"
            val params = arrayOf(_cocktailId.toString())
            // SQL実行
            val cursor = db.rawQuery(sql, params)

            // データベースから取得した値を格納する変数の用意
            // データが無かった時のための初期値も用意
            var note = ""
            // SQL実行の戻り値であるカーソルオブジェクトをループさせてデータベース内のデータを取得
            while (cursor.moveToNext()) {
                // カラムのインデックス値を取得
                val idxNote = cursor.getColumnIndex("note")
                // カラムのインデックス値を元に実際のデータを取得
                note = cursor.getString(idxNote)
            }
            // カーソルを解放
            cursor.close()

            // 感想のEditTextの各画面部品を取得しデータベースの値を反映
            val etNote = findViewById<EditText>(R.id.etNote)
            etNote.setText(note)
        }
    }
}
