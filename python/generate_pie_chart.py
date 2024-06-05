import plotly.graph_objects as go
import sys


def generate_pie_chart(values, labels, output_file, balance, width, height):
    fig = go.Figure(data=[go.Pie(labels=labels, values=values,
                                 textinfo='label+value+percent',
                                 hole=.3,
                                 textfont=dict(size=24)
                                 )])

    (btext, bcolor) = ('+ %d' % balance, 'green') if (balance > 0) else ('- %d' % abs(balance), 'red')

    fig.update(layout_showlegend=False)
    fig.update_layout(
        width=width,
        height=height,
        margin=dict(t=10, l=10, r=10, b=10),
        annotations=[dict(text=btext, x=0.5, y=0.5, font_size=36, showarrow=False, font=dict(color=bcolor))]
    )

    fig.write_image(output_file, format='png')


if __name__ == "__main__":
    values = list(map(float, sys.argv[1].strip('[]').split(';')))
    labels = sys.argv[2].strip('[]').split(';')
    output_file = sys.argv[3]
    balance = int(sys.argv[4])
    width = int(sys.argv[5]) if len(sys.argv) > 5 else 1024
    height = int(sys.argv[6]) if len(sys.argv) > 6 else 1024
    generate_pie_chart(values, labels, output_file, balance, width, height)
