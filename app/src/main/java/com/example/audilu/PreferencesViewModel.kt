import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PreferencesViewModel : ViewModel() {

    // LiveData para switches
    private val _switchVibState = MutableLiveData<Int>(1)
    val switchVibState: LiveData<Int> get() = _switchVibState

    private val _switchFlashState = MutableLiveData<Int>(1)
    val switchFlashState: LiveData<Int> get() = _switchFlashState

    // LiveData para checkboxes de vibración
    private val _cbVibSonState = MutableLiveData<Int>(1)
    val cbVibSonState: LiveData<Int> get() = _cbVibSonState

    private val _cbVibMovState = MutableLiveData<Int>(0)
    val cbVibMovState: LiveData<Int> get() = _cbVibMovState

    private val _cbVibGasState = MutableLiveData<Int>(0)
    val cbVibGasState: LiveData<Int> get() = _cbVibGasState

    // LiveData para checkboxes de luz
    private val _cbLuzSonState = MutableLiveData<Int>(1)
    val cbLuzSonState: LiveData<Int> get() = _cbLuzSonState

    private val _cbLuzMovState = MutableLiveData<Int>(0)
    val cbLuzMovState: LiveData<Int> get() = _cbLuzMovState

    private val _cbLuzGasState = MutableLiveData<Int>(0)
    val cbLuzGasState: LiveData<Int> get() = _cbLuzGasState

    // Métodos para actualizar el estado
    fun setSwitchVibState(state: Int) {
        _switchVibState.value = state
    }

    fun setSwitchFlashState(state: Int) {
        _switchFlashState.value = state
    }

    fun setCbVibSonState(state: Int) {
        _cbVibSonState.value = state
    }

    fun setCbVibMovState(state: Int) {
        _cbVibMovState.value = state
    }

    fun setCbVibGasState(state: Int) {
        _cbVibGasState.value = state
    }

    fun setCbLuzSonState(state: Int) {
        _cbLuzSonState.value = state
    }

    fun setCbLuzMovState(state: Int) {
        _cbLuzMovState.value = state
    }

    fun setCbLuzGasState(state: Int) {
        _cbLuzGasState.value = state
    }
}
