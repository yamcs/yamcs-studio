from docutils import nodes


class color(nodes.General, nodes.TextElement):
    pass


def visit_color_node_html(self, node):
    self.body.append(
        """<span style="border: 1px solid #000; background-color: %s">
            &nbsp;&nbsp;
        </span>&nbsp;&nbsp;"""
        % node.astext()
    )


def depart_color_node_html(self, node):
    pass


def visit_color_node_latex(self, node):
    pass


def depart_color_node_latex(self, node):
    pass


def color_role(name, rawtext, text, lineno, inliner, options={}, content=[]):
    node = color()
    node += nodes.Text(text)
    return [node], []


def setup(app):
    app.add_role("color", color_role)
    app.add_node(
        color,
        html=(visit_color_node_html, depart_color_node_html),
        latex=(visit_color_node_latex, depart_color_node_latex),
    )
