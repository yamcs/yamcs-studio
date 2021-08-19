from docutils import nodes
from sphinx.util.docutils import SphinxDirective


class OpiDirective(SphinxDirective):
    required_arguments = 1
    has_content = True

    def run(self):
        container = nodes.container()
        container["classes"].append("opi")

        content = nodes.container()
        content["classes"].append("content")

        display_container = nodes.container()
        display_container["classes"].append("display")

        img_node = nodes.image()
        img_node["align"] = "center"
        img_node["uri"] = self.arguments[0]
        display_container += img_node
        
        content += display_container

        if self.content:
            node = nodes.Element()  # Anonymous container for parsing
            self.state.nested_parse(self.content, self.content_offset, node)

            table_data = [[item.children for item in row_list[0]] for row_list in node[0]]

            table_node = nodes.table()
            tgroup = nodes.tgroup(cols=2, colwidths="auto")
            table_node += tgroup
            for col_width in [50, 50]:
                colspec = nodes.colspec()
                colspec.attributes["colwidth"] = col_width
                tgroup += colspec

            thead = nodes.thead()
            header_row_node = nodes.row()
            entry = nodes.entry()
            entry += nodes.Text("Property")
            header_row_node += entry
            entry = nodes.entry()
            entry += nodes.Text("Value")
            header_row_node += entry
            thead += header_row_node
            tgroup += thead
            
            tbody = nodes.tbody()
            for row in table_data:
                row_node = nodes.row()
                for cell in row:
                    entry = nodes.entry()
                    entry += cell
                    row_node += entry
                tbody += row_node
            tgroup += tbody

            properties_container = nodes.container()
            properties_container["classes"].append("properties")
            properties_container += table_node

            content += properties_container

        container += content
        return [container]


def setup(app):
    app.add_directive("opi", OpiDirective)
