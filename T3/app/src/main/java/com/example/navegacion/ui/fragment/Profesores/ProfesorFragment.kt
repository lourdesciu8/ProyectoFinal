import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.navegacion.databinding.FragmentProfesoresMainBinding
import com.example.navegacion.ui.adapter.AgendaAdapter
import com.example.navegacion.ui.fragment.Profesores.MenuDialogFragment
import com.example.navegacion.ui.model.profesor.AgendaItem

class ProfesorFragment: Fragment() {
    private lateinit var binding: FragmentProfesoresMainBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfesoresMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Crear una lista de objetos AgendaItem
        val agendaList = listOf(
            AgendaItem("1:00 PM", "Clase en el aula 101"),
            AgendaItem("3:00 PM", "Reunión con el equipo de trabajo"),
            AgendaItem("4:30 PM", "Clase en el aula 102")
        )

        // Inicializar el adaptador con la lista de datos
        var agendaAdapter = AgendaAdapter(agendaList)

        // Configurar el RecyclerView con el adaptador
        binding.rvAgenda.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAgenda.adapter = agendaAdapter
//aqui gestiono el dialogo del menu desplegable.
        binding.iconoDesplegable.setOnClickListener {
            val menuDialog = MenuDialogFragment()
            menuDialog.show(parentFragmentManager, "MenuDialogFragment")
        }
    }
}