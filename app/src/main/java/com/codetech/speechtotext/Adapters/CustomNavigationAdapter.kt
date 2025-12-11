import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codetech.speechtotext.databinding.NavMenuItemBinding

class CustomNavigationAdapter(
    private val context: Context,
    private val items: List<NavigationItem>,
    private val onItemClick: (NavigationItem) -> Unit
) : RecyclerView.Adapter<CustomNavigationAdapter.ViewHolder>() {

    data class NavigationItem(
        val id: Int,
        val title: String,
        val icon: Int
    )

    class ViewHolder(val binding: NavMenuItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NavMenuItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.menuItemText.apply {
            text = item.title
            setCompoundDrawablesWithIntrinsicBounds(item.icon, 0, 0, 0)
        }
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size
} 