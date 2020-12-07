package li.kta.espguard.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import li.kta.espguard.R

class AddSensorActivity : AppCompatActivity() {
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_add_sensor)
    setSupportActionBar(findViewById(R.id.toolbar_support_add))
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_toolbar, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == R.id.open_settings) {
      startActivity(Intent(this, SettingsActivity::class.java))
      return true
    }
    return super.onOptionsItemSelected(item)
  }
}