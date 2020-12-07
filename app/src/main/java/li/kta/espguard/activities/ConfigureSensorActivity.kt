package li.kta.espguard.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import li.kta.espguard.R

class ConfigureSensorActivity : AppCompatActivity() {

  companion object {
    val TAG = ConfigureSensorActivity::class.java.name
    const val EXTRA_SENSOR_ID = "sensorId"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_configure_sensor)
    setSupportActionBar(findViewById(R.id.toolbar_support_configure))
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